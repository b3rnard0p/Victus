DELIMITER $$

DROP PROCEDURE IF EXISTS InsertFichasLote$$

CREATE PROCEDURE InsertFichasLote(IN p_nutricionista_id BIGINT, IN p_quantidade INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_prep_id BIGINT;
    DECLARE v_perf_id BIGINT;
    DECLARE v_ficha_id BIGINT;
    DECLARE v_ing_id BIGINT;
    DECLARE v_ref_id BIGINT;
    
    WHILE i < p_quantidade DO
        
        -- 1. Inserir Ingrediente (1 por loop)
        INSERT INTO ingrediente (nome, ptn, cho, lip, sodio, gordura_saturada, status, usuario_id)
        VALUES (CONCAT('Ingrediente Teste Carga ', i, ' - ', UUID_SHORT()), 10.00, 20.00, 5.00, 1.00, 0.50, 'ATIVA', p_nutricionista_id);
        SET v_ing_id = LAST_INSERT_ID();

        -- 2. Inserir Preparação
        INSERT INTO preparacao (
            nome, categoria, numero, tempo_preparo, equipamentos, modo_preparo, 
            qntd_agua, porcent_agua, fcc, rendimento
        )
        VALUES (
            CONCAT('Ficha de Teste de Carga ', i, ' - ', UUID_SHORT()), 
            'PRATOPRINCIPAL', 
            IF((i % 9999) = 0, 1, i % 9999), 
            '30 min', 
            'Forno, Panela', 
            'Misturar tudo e cozinhar.', 
            150.00, 
            15.00, 
            1.20, 
            500.00
        );
        SET v_prep_id = LAST_INSERT_ID();
        
        -- 3. Inserir Perfil Nutricional
        INSERT INTO perfil_nutricional (
            vtc, kcalptn, kcalcho, kcallip, 
            gramasptn, gramascho, gramaslip, 
            gramas_sodio, gramas_saturada, 
            porcentptn, porcentcho, porcentlip
        )
        VALUES (
            500.00, 100.00, 300.00, 100.00, 
            25.00, 75.00, 11.00, 
            2.00, 1.50, 
            20.00, 60.00, 20.00
        );
        SET v_perf_id = LAST_INSERT_ID();
        
        -- 4. Inserir Ficha Técnica
        INSERT INTO ficha_tecnica (
            custo_per_capita, custo_total, numero_porcoes, peso_porcao, medida_caseira, 
            status, status_criacao, nutricionista_id, preparacao_id, perfil_nutricional_id
        )
        VALUES (
            15.50, 
            155.00, 
            10, 
            250.00, 
            '1 porção generosa', 
            'ATIVA', 
            'COMPLETA', 
            p_nutricionista_id, 
            v_prep_id, 
            v_perf_id
        );
        SET v_ficha_id = LAST_INSERT_ID();
        
        -- 5. Vincular o Ingrediente gerado na Ficha
        INSERT INTO ingredientes_por_ficha (
            medida_caseira, pb, pl, fc, custo_usado, custokg, 
            ptn_calculado, cho_calculado, lip_calculado, sodio_calculado, 
            gordura_saturada_calculada, ficha_tecnica_id, ingrediente_id, perfil_nutricional_id
        )
        VALUES (
            '1 colher', 50.00, 45.00, 1.11, 2.50, 50.00, 
            5.00, 10.00, 2.50, 0.50, 
            0.25, v_ficha_id, v_ing_id, v_perf_id
        );

        -- 6. Inserir Refeição (1 por loop)
        INSERT INTO refeicao (
            nome, kcal_total, total_gramasptn, total_gramascho, total_gramaslip, 
            total_gramas_sodio, total_gramas_saturada, status, nutricionista_id
        )
        VALUES (
            CONCAT('Refeição Teste Carga ', i, ' - ', UUID_SHORT()), 
            '500 kcal', 
            25.00, 75.00, 11.00, 
            2.00, 1.50, 
            'ATIVA', p_nutricionista_id
        );
        SET v_ref_id = LAST_INSERT_ID();

        -- 7. Vincular Ficha na Refeição
        INSERT INTO fichas_por_refeicao (refeicao_id, ficha_tecnica_id)
        VALUES (v_ref_id, v_ficha_id);
        
        SET i = i + 1;
    END WHILE;
END$$

DELIMITER ;
