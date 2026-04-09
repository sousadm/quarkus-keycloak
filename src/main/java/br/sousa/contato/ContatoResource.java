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

    @GET
    @Path("/{id}")
    public Contato buscarPorId(@PathParam("id") Long id) {
        Contato contato = Contato.findById(id);
        if (contato == null) {
            throw new WebApplicationException("Contato não encontrado", 404);
        }
        return contato;
    }

    @POST
    @Transactional
    public Contato adicionar(Contato contato) {
    	contato.ativo = Boolean.TRUE;
        contato.persist();
        return contato;
    }
    
    @PUT
    @Path("/{id}")
    @Transactional
    public Contato atualizar(@PathParam("id") Long id, Contato dados) {
        Contato contato = Contato.findById(id);
        if (contato == null) {
            throw new WebApplicationException("Contato não encontrado", 404);
        }
        contato.nome = dados.nome;
        contato.fone = dados.fone;
        return contato;
    } 
    
    @PUT
    @Path("/{id}/inativar")
    @Transactional
    public Contato inativar(@PathParam("id") Long id) {
        Contato contato = Contato.findById(id);
        if (contato == null) {
            throw new WebApplicationException("Contato não encontrado", 404);
        }
        contato.ativo = false;
        return contato;
    }

    @PUT
    @Path("/{id}/ativar")
    @Transactional
    public Contato ativar(@PathParam("id") Long id) {
        Contato contato = Contato.findById(id);
        if (contato == null) {
            throw new WebApplicationException("Contato não encontrado", 404);
        }
        contato.ativo = true;
        return contato;
    }    
    
}
