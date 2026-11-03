package dev.portfolio.requestdesk.ticket;

import java.time.Instant;

import dev.portfolio.requestdesk.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

// заявка
@Entity
@Table(name = "tickets")
public class Ticket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Version
    @Column(nullable = false)
    private long version;
    @Column(nullable=false, length=120)
    private String title;
    @Column(nullable=false, length=2000)
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16)
    private TicketPriority priority;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private TicketStatus status = TicketStatus.OPEN;

    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="creator_id")
    private AppUser creator;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="assignee_id")
    private AppUser assignee;

    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;
    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;


    protected Ticket() {}
    public Ticket(String title, String description, TicketPriority priority, AppUser creator) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.creator = creator;
    }
    @PrePersist
    void created() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }
    @PreUpdate void updated() { updatedAt=Instant.now(); }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public AppUser getCreator() { return creator; }
    public AppUser getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void assignTo(AppUser agent) {
        this.assignee = agent;
        this.status = TicketStatus.IN_PROGRESS;
    }
    public void setStatus(TicketStatus status) { this.status=status; }
}
