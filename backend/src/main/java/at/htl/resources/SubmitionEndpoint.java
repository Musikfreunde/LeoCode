package at.htl.resources;

import at.htl.entities.LeocodeStatus;
import at.htl.entities.Submition;
import at.htl.repositories.SubmitionRepository;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.groups.MultiSubscribe;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.SseElementType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseEventSink;

@Path("/submition")
public class SubmitionEndpoint {

    @Inject
    @Channel("submition-result")
    Multi<Submition> results;

    @Inject
    SubmitionRepository submitionRepository;

    @Inject
    Logger log;

    @GET
    @Path("/{id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType("text/plain")
    public void stream(@Context Sse sse, @Context SseEventSink sseEventSink, @PathParam("id") Long id) {
        Submition currentSubmition = submitionRepository.findById(id);
        boolean canSubscribe = false;

        // When someone refreshes or connects later on send them the current status
        if (currentSubmition != null) {
            sseEventSink.send(sse.newEvent(currentSubmition.status.toString()));
            // anything other than SUBMITTED is complete
            canSubscribe = currentSubmition.status == LeocodeStatus.SUBMITTED;
        }

        // Only allow sse if the submition is not complete
        if (canSubscribe) {
            log.info("subscribed to Submition SSE");
            MultiSubscribe<Submition> subscribe = results.subscribe();

            subscribe.with(submition -> {
                if (id.equals(submition.id)) {
                    LeocodeStatus status = submition.status;
                    sseEventSink.send(sse.newEvent(status.toString()));

                    if (status != LeocodeStatus.SUBMITTED) {
                        log.info("closed SSE");
                        sseEventSink.close();
                    }
                }
            });
        }
    }
}
