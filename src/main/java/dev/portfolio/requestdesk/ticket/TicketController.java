package dev.portfolio.requestdesk.ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    private final TicketService service;
    public TicketController(TicketService service) { this.service=service; }

    public record NewTicket(@NotBlank @Size(max=120) String title,
        @NotBlank @Size(max=2000) String description,
        @NotNull TicketPriority priority) {}
    public record StatusUpdate(@NotNull TicketStatus status) {}
    public record NewComment(@NotBlank @Size(max=1000) String body) {}

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public TicketService.Summary create(@Valid @RequestBody NewTicket request) {
        return service.create(new TicketService.CreateTicket(request.title(),request.description(),request.priority()));
    }
    @GetMapping
    public Page<TicketService.Summary> list(@RequestParam(defaultValue="0") int page,
        @RequestParam(defaultValue="20") int size) {
        return service.list(page, size);
    }
    @GetMapping("/{id}")
    public TicketService.Details get(@PathVariable long id) { return service.get(id); }

    @PostMapping("/{id}/take")
    public TicketService.Summary take(@PathVariable long id) { return service.take(id); }

    @PatchMapping("/{id}/status")
    public TicketService.Summary changeStatus(@PathVariable long id,@Valid @RequestBody StatusUpdate request) {
        return service.changeStatus(id,request.status());
    }

    @PostMapping("/{id}/comments") @ResponseStatus(HttpStatus.CREATED)
    public TicketService.CommentView comment(@PathVariable long id,@Valid @RequestBody NewComment request) {
        return service.comment(id,request.body());
    }
}
