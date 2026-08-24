package com.example.sistemanutricao.aop;

import com.example.sistemanutricao.model.enuns.TipoAcao;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegistrarAcao {
    TipoAcao acao();
}
