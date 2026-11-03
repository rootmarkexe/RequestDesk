package dev.portfolio.requestdesk.ticket;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
