package dev.portfolio.requestdesk.ticket;

import java.time.Instant;

import dev.portfolio.requestdesk.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name="ticket_comments")
public class TicketComment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="ticket_id")
    private Ticket ticket;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="author_id")
    private AppUser author;
    @Column(nullable=false, length=1000)
    private String body;
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    protected TicketComment() {}
    public TicketComment(Ticket ticket, AppUser author, String body){
        this.ticket=ticket;
        this.author=author;
        this.body=body;
    }
    @PrePersist void created(){
        this.createdAt=Instant.now(); 
    }

    public Long getId() { return id; }
    public AppUser getAuthor() { return author; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
}
