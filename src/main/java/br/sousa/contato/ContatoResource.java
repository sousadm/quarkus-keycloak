package br.sousa.contato;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/contatos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContatoResource {

    @GET
    public List<Contato> listar() {
        return Contato.listAll();
    }

    @POST
    @Transactional
    public Contato adicionar(Contato contato) {
        contato.persist();
        return contato;
    }
}
