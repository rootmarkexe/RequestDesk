package dev.portfolio.requestdesk.ticket;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import dev.portfolio.requestdesk.security.AuthService;
import dev.portfolio.requestdesk.user.AppUser;
import dev.portfolio.requestdesk.user.Role;

@Service
public class TicketService{
    private final TicketRepository tickets;
    private final CommentRepository comments;
    private final AuthService auth;
    public TicketService(TicketRepository tickets, CommentRepository comments, AuthService auth){
        this.tickets = tickets;
        this.comments = comments;
        this.auth = auth;
    }

    public record CreateTicket(String title, String description, TicketPriority priority){}
    public record Summary(Long id, String title, TicketPriority priority, TicketStatus status,
                          String creator, String assignee, Instant createdAt){}
    public record CommentView(Long id, String author, String body, Instant createdAt){}
    public record Details(Long id, String title, String description, TicketPriority priority, TicketStatus status,
                          String creator, String assignee, Instant createdAt, List<CommentView> comments){}

    @Transactional
    public Summary create(CreateTicket request){
        AppUser user = auth.currentUser();
        if (user.getRole() != Role.USER) throw forbidden();
        return summary(tickets.save(new Ticket(request.title().trim(), request.description().trim(), request.priority(), user)));
    }
    @Transactional(readOnly=true)
    public Page<Summary> list(int page, int size){
        AppUser user=auth.currentUser();
        if (page < 0 || size<1 || size > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректная пагинация");

        Pageable paging = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC,"createdAt","id"));
        Page<Ticket> result = user.getRole()==Role.AGENT ? tickets.findAll(paging) : tickets.findByCreatorId(user.getId(),paging);
        return result.map(TicketService::summary);
    }
    @Transactional(readOnly=true)
    public Details get(long id){
        AppUser user=auth.currentUser();
        Ticket ticket=visibleTicket(id,user);

        List<CommentView> ticketComments = comments.findByTicketIdOrderByCreatedAtAsc(id).stream()
            .map(c -> new CommentView(c.getId(), c.getAuthor().getEmail(), c.getBody(), c.getCreatedAt())).toList();
       
       
        return new Details(ticket.getId(),ticket.getTitle(),ticket.getDescription(),ticket.getPriority(),
            ticket.getStatus(),ticket.getCreator().getEmail(),
            ticket.getAssignee()==null?null:ticket.getAssignee().getEmail(), ticket.getCreatedAt(), ticketComments);
    }
    @Transactional
    public Summary take(long id){
        AppUser user=auth.currentUser();
        requireAgent(user);

        Ticket ticket=visibleTicket(id,user);
        if (ticket.getStatus()==TicketStatus.RESOLVED) throw new ResponseStatusException(HttpStatus.CONFLICT,"Заявка уже закрыта");
        if (ticket.getAssignee()!=null && !ticket.getAssignee().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Заявка уже назначена другому оператору");

        ticket.assignTo(user);
        return summary(ticket);
    }
    @Transactional
    public Summary changeStatus(long id, TicketStatus status) {
        AppUser user=auth.currentUser();
        requireAgent(user);

        Ticket ticket=visibleTicket(id,user);
        if (ticket.getAssignee()==null || !ticket.getAssignee().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Сначала возьмите заявку в работу");
        if (ticket.getStatus()==TicketStatus.RESOLVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Закрытую заявку нельзя открыть повторно");
        if (status==TicketStatus.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Открыть повторно в демо не поддерживается");

        ticket.setStatus(status);
        return summary(ticket);
    }
    @Transactional
    public CommentView comment(long id, String body) {
        AppUser user=auth.currentUser();
        Ticket ticket=visibleTicket(id,user);
        if (ticket.getStatus()==TicketStatus.RESOLVED)
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Нельзя комментировать закрытую заявку");

        TicketComment saved=comments.save(new TicketComment(ticket,user,body.trim()));
        return new CommentView(saved.getId(), user.getEmail(), saved.getBody(), saved.getCreatedAt());
    }


    private Ticket visibleTicket(long id, AppUser user) {
        Ticket ticket=tickets.findById(id).orElseThrow(TicketService::notFound);
        if (user.getRole()!=Role.AGENT && !ticket.getCreator().getId().equals(user.getId())) throw notFound();
        return ticket;
    }
    private static Summary summary(Ticket ticket) {
        return new Summary(ticket.getId(),ticket.getTitle(),ticket.getPriority(),ticket.getStatus(),
            ticket.getCreator().getEmail(),ticket.getAssignee()==null?null:ticket.getAssignee().getEmail(),ticket.getCreatedAt());
    }
    private static void requireAgent(AppUser user) { if (user.getRole()!=Role.AGENT) throw forbidden(); }
    private static ResponseStatusException forbidden() {return new ResponseStatusException(HttpStatus.FORBIDDEN,"Нет прав оператора");}
    private static ResponseStatusException notFound() {return new ResponseStatusException(HttpStatus.NOT_FOUND,"Заявка не найдена");}
}
