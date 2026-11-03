package dev.portfolio.requestdesk.ticket;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByCreatorId(Long creatorId, Pageable pageable);
}
