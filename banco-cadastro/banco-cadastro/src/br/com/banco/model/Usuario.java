package br.com.banco.model;

import java.time.LocalDate;

/**
 * Entidade que representa um cliente do banco.
 *
 * Ela só guarda dados. Quem decide se os dados são válidos
 * é o UsuarioService (regras de negócio).
 */
public class Usuario {

    private Integer id;
    private String nomeCompleto;
    private String cpf;              // guardado só com dígitos: "52998224725"
    private LocalDate dataNascimento;
    private String email;
    private String telefone;         // guardado só com dígitos: "92991234567"
    private String endereco;
    private String senha;            // ATENÇÃO: em um sistema real, a senha nunca é salva em texto puro (ver README)

    public Usuario(String nomeCompleto, String cpf, LocalDate dataNascimento,
                   String email, String telefone, String endereco, String senha) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.email = email;
        this.telefone = telefone;
        this.endereco = endereco;
        this.senha = senha;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        // A senha fica de fora de propósito: nunca imprima senhas em logs.
        return "Usuario{id=" + id + ", nome='" + nomeCompleto + "', cpf='" + cpf + "', email='" + email + "'}";
    }
}
