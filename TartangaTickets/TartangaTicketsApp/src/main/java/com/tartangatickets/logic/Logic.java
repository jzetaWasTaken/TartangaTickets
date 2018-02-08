/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tartangatickets.logic;

import com.tartangatickets.entities.Credential;
import com.tartangatickets.entities.Department;
import com.tartangatickets.entities.Message;
import com.tartangatickets.entities.State;
import com.tartangatickets.entities.Ticket;
import com.tartangatickets.entities.User;
import com.tartangatickets.exceptions.NotSecureException;
import com.tartangatickets.exceptions.ReadException;
import com.tartangatickets.utils.EmailSender;
import com.tartangatickets.utils.HibernateUtil;
import com.tartangatickets.utils.PasswordHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import java.util.logging.Level;
import javax.persistence.NoResultException;

/**
 *
 * @author ubuntu
 */
public class Logic implements LogicInterface {
    
    private static final Logger LOGGER = Logger.getLogger("com.tartangatickets.logic");
    private final SessionFactory factory = HibernateUtil.getSessionFactory();
    private final Session session = factory.openSession();
    private Transaction tx = null;
    private final HashMap sessionContent = new HashMap<>();
    
    @Override
    public HashMap getSessionContent() {
        return sessionContent;
    }
    
    @Override
    public void createTicket(Ticket ticket) throws Exception {
        LOGGER.info("Creating ticket");
        try {
            tx = session.beginTransaction();
            ticket.setCreateDate(new Date());
            session.persist(ticket);
            tx.commit();
            // TODO Send email
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception creating ticket. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.info("Ticket created");
    }

    @Override
    public void sendMessage(Message message) throws Exception {
        LOGGER.info("Creating ticket message");
        try {
            tx = session.beginTransaction();
            //session.persist(message);
            Ticket ticket = message.getTicket();
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            ticket.setMessages(messages);
            session.merge(ticket);
            // TODO send email
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception creating ticket message. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.info("Ticket message created");
    }

    @Override
    public List<Ticket> findTicketsByUser(String userLogin) throws Exception {
        LOGGER.info("Fetching tickets by user");
        List<Ticket> tickets = null;
        try {
            tx = session.beginTransaction();
            tickets = session.createNamedQuery("findTicketsByUser")
                    .setParameter("login", userLogin)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception finding tickets by user. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} tickets found",
                tickets.size());
        return tickets;
    }

    @Override
    public List<Ticket> findAllTickets() throws Exception {
        LOGGER.info("Fetching all tickets");
        List<Ticket> tickets = null;
        try {
            tx = session.beginTransaction();
            tickets = session.createNamedQuery("findAllTickets")
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception finding tickets. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} tickets found",
                tickets.size());
        return tickets;
    }

    @Override
    public void changePassword(Credential credential, String newPassword) throws Exception {
        LOGGER.info("Changing user password");
        try {
            if (PasswordHandler.checkSecurity(newPassword)) {
                tx = session.beginTransaction();
                String passwordHash = PasswordHandler
                        .getHash(newPassword, credential.getLogin());
                credential.setPassword(passwordHash);
                session.merge(credential);
                tx.commit();
            } else {
                LOGGER.warning("Password not secure");
                throw new NotSecureException("Password not secure");
            } 
        } catch (NotSecureException e) {
            LOGGER.warning(e.getMessage());
            throw new NotSecureException();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception changinf password. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
    }

    @Override
    public void recoverPassword(String login) throws Exception {
        LOGGER.info("Recovering user password");
        try {
            tx = session.beginTransaction();
            User user = (User) session.createNamedQuery("findUserById")
                .setParameter("login", login)
                .getSingleResult();
            String newPassword = setPassword(user);
            session.merge(user);
            tx.commit();
            LOGGER.info("Sending email");
            EmailSender.sendEmail(login, newPassword);
        } catch (NoResultException e) {
            LOGGER.log(Level.WARNING,
                    "No user with login {0}",
                    login);
            tx.rollback();
            throw new ReadException();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error finding user. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.info("Password recovery successful");
    }
    
    private String setPassword(User user) throws Exception {
        LOGGER.info("Setting new password");
        String login = user.getCredential().getLogin();
        String newPassword = PasswordHandler.generatePassword();
        LOGGER.info("Setting user credentials");
        String passwordHash = PasswordHandler.getHash(newPassword, login);
        user.getCredential().setPassword(passwordHash);
        user.getCredential().setLastPassChange(new Date());
        return newPassword;
    }

    @Override
    public User createUser(User user) throws Exception {
        LOGGER.info("Creating user");
        try {
            tx = session.beginTransaction();
            String newPassword = setPassword(user);
            session.persist(user);
            tx.commit();
            LOGGER.info("Sending email");
            EmailSender.sendEmail(user.getCredential().getLogin(), newPassword);
        } catch (Exception e) {
            tx.rollback();
            LOGGER.log(Level.SEVERE,
                    "Error creating user. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.info("User created");
        return user;
    }

    @Override
    public void deleteUser(User user) throws Exception {
        LOGGER.info("Deleting user");
        try {
            tx = session.beginTransaction();
            session.remove(user);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            LOGGER.log(Level.SEVERE,
                    "Error deleting user. {0}",
                    e.getMessage());
            throw new Exception();
        }
        LOGGER.info("User deleted");
    }

    @Override
    public List<User> findAllUsers() throws Exception {
        LOGGER.info("Fetching all users");
        List<User> users = null;
        try {
            tx = session.beginTransaction();
            users = session.createNamedQuery("findAllUsers")
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Error fetching users. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} users found",
                users.size());
        return users;
    }

    @Override
    public void assignTicket(Ticket ticket) throws Exception {
        LOGGER.info("Assigning ticket");
        try {
            tx = session.beginTransaction();
            session.merge(ticket);
            User technician = ticket.getTechnician();
            session.merge(technician);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            LOGGER.log(Level.SEVERE,
                "Error assigning ticket. {0}",
                e.getMessage());
            throw new Exception();
        }
        LOGGER.info("Ticket assigned");
    }

    @Override
    public void changeState(Ticket ticket) throws Exception {
        LOGGER.info("Changing ticket state");
        try {
            tx = session.beginTransaction();
            session.merge(ticket);
            tx.commit();
            // TODO send message to user
        } catch (Exception e) {
            tx.rollback();
            LOGGER.log(Level.SEVERE,
                "Error changign ticket state. {0}",
                e.getMessage());
            throw new Exception();
        }
        LOGGER.info("Ticket state changed");
    }

    @Override
    public User authenticate(String login, String password) throws Exception {
        LOGGER.info("Authenticating user");
        User user = null;
        try {
            String passwordHash = PasswordHandler.getHash(password, login);
            user = (User) session.createNamedQuery("findUserByLogin")
                    .setParameter("login", login)
                    .setParameter("password", passwordHash)
                    .getSingleResult();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                "Error authenticating user. {0}",
                e.getMessage());
            throw new Exception();
        }
        LOGGER.info("Log in successful");
        return user;
    }   
    
    /*
    @Override
    public void createTechnician(Technician technician) throws Exception {
        
    }

    @Override
    public void deleteTechnician(Technician technician) throws Exception {
        
    }

    @Override
    public void updateTechnician(Technician technician) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    */

    @Override
    public List<Department> findAllDepartments() throws Exception {
        LOGGER.info("Fetching department by name");
        List<Department> departments = null;
        try {
            tx = session.beginTransaction();
            departments = session.createNamedQuery("findAllDepartments")
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            LOGGER.log(Level.SEVERE,
                "Error afetching departments. {0}",
                e.getMessage());
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} deparments found",
                departments.size());
        return departments;
    }

    @Override
    public List<Ticket> findTicketsByState(State state) throws Exception {
        LOGGER.info("Fetching tickets by state");
        List<Ticket> tickets = null;
        try {
            tx = session.beginTransaction();
            tickets = session.createNamedQuery("findTicketsByState")
                    .setParameter("state", state)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception finding tickets by state. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} tickets found",
                tickets.size());
        return tickets;
    }

    @Override
    public List<Ticket> findTicketsByTechnician(String login) throws Exception {
        LOGGER.info("Fetching tickets by technician");
        List<Ticket> tickets = null;
        try {
            tx = session.beginTransaction();
            tickets = session.createNamedQuery("findTicketsByTechnician")
                    .setParameter("login", login)
                    .getResultList();
            tx.commit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,
                    "Exception finding tickets by technician. {0}",
                    e.getMessage());
            tx.rollback();
            throw new Exception();
        }
        LOGGER.log(Level.INFO,
                "{0} tickets found",
                tickets.size());
        return tickets;
    }
    
}
