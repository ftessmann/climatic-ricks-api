package com.climaticrisks.models;

public class Usuario extends BaseModel {
    private String nome;
    private String email;
    private Endereco endereco;
    private String senha;
    private String telefone;
    private Boolean isDefesaCivil;

    public Usuario() {
        super();
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public Boolean getDefesaCivil() { return isDefesaCivil; }
    public void setDefesaCivil(Boolean defesaCivil) { isDefesaCivil = defesaCivil; }
}
