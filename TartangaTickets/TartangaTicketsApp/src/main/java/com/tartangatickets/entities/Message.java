package com.tartangatickets.entities;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *  Encapsulates the data of the message.
 *  <ul>
 *      <li><strong>id:</strong> Identification number.</li>
 *      <li><strong>body:</strong> Ticket body.</li>
 *      <li><strong>ticket:</strong> Ticket end date.</li>
 *      <li><strong>user:</strong> Ticket creator.</li>
 *  </ul>
 *
 *  @author Sergio López, Iker Jon Mediavilla, Ionut Savin, Jon Zaballa
 *  @version 1.0, Feb 21 2018
 */

@Entity(name="Message")
@Table(name="messages", schema="tartanga_ticket_db")
public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String body;
    @ManyToOne
    private Ticket ticket;
    @ManyToOne
    private User user;

    public Message() {
    }

    public Message(String body, Ticket ticket, User user) {
        this.body = body;
        this.ticket = ticket;
        this.user = user;
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Message)) {
            return false;
        }
        Message other = (Message) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.tartangatickets.entities.Message[ id=" + id + " ]";
    }
    
}
