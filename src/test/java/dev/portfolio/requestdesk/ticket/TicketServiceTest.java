package dev.portfolio.requestdesk.ticket;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import dev.portfolio.requestdesk.security.AuthService;
import dev.portfolio.requestdesk.user.AppUser;
import dev.portfolio.requestdesk.user.Role;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest{
    @Mock TicketRepository tickets;
    @Mock CommentRepository comments;
    @Mock AuthService auth;
    @InjectMocks TicketService service;

    @Test void userCannotTakeTicket(){
        AppUser user=mock(AppUser.class);
        when(user.getRole()).thenReturn(Role.USER);
        when(auth.currentUser()).thenReturn(user);

        assertThrows(ResponseStatusException.class, () -> service.take(1L));
        verifyNoInteractions(tickets);
    }

    @Test void userCannotReadOtherUsersTicket(){
        AppUser user=mock(AppUser.class);
        AppUser owner=mock(AppUser.class);
        when(user.getRole()).thenReturn(Role.USER);
        when(user.getId()).thenReturn(1L);
        when(owner.getId()).thenReturn(2L);
        when(auth.currentUser()).thenReturn(user);

        Ticket ticket = new Ticket("Проблема", "Описание", TicketPriority.NORMAL, owner);
        when(tickets.findById(3L)).thenReturn(Optional.of(ticket));

        assertThrows(ResponseStatusException.class, () -> service.get(3L));
        verifyNoInteractions(comments);
    }
}
