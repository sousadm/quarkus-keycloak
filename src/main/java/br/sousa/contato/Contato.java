package br.sousa.contato;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Contato extends PanacheEntity {
    public String nome;
    public String fone;
    public Boolean ativo;
}
