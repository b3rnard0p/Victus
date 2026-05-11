package com.example.sistemanutricao.security;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;

public class UsuarioSecurity implements UserDetails {

    private final Usuario usuario;

    public UsuarioSecurity(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + usuario.getCargo().name();
        return List.of(new SimpleGrantedAuthority(role));
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getEstabelecimentoId() {
        return usuario.getEstabelecimento().getId();
    }

    @Override
    public String getPassword() {
        return usuario.getSenha();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    public String getCaminhoImagem(){return usuario.getCaminhoImagem();}

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isAtivo();
    }

    public Long getId() {
        return usuario.getId();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public Cargo getCargo() {
        return usuario.getCargo();
    }
}
