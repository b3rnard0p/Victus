package com.example.sistemanutricao.service.taco;

import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.sistemanutricao.repository.IngredienteRepository;
import com.example.sistemanutricao.repository.specification.IngredienteSpecification;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class TacoInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TacoInitializationService.class);
    
    @Value("${app.taco.username}")
    private String tacoUsername;

    @Value("${app.taco.email}")
    private String tacoEmail;

    @Value("${app.taco.password}")
    private String tacoPassword;

    @Value("${app.taco.import-on-startup:true}")
    private boolean importOnStartup;

    private static final String TACO_CLASSPATH_FILE = "bootstrap/Taco.xlsx";
    private static final String ACIDOS_CLASSPATH_FILE = "bootstrap/Acidos.xlsx";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TacoFileReader tacoFileReader;
    private final TacoExcelParser tacoExcelParser;
    private final TacoIngredientePersister tacoIngredientePersister;
    private final IngredienteRepository ingredienteRepository;

    public TacoInitializationService(UsuarioRepository usuarioRepository,
                                     PasswordEncoder passwordEncoder,
                                     TacoFileReader tacoFileReader,
                                     TacoExcelParser tacoExcelParser,
                                     TacoIngredientePersister tacoIngredientePersister,
                                     IngredienteRepository ingredienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tacoFileReader = tacoFileReader;
        this.tacoExcelParser = tacoExcelParser;
        this.tacoIngredientePersister = tacoIngredientePersister;
        this.ingredienteRepository = ingredienteRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (!importOnStartup) {
                logger.info("Importação da TACO desativada. Pulando inicialização.");
                return;
            }

            Usuario tacoUsuario = garantirUsuarioTaco();
            
            if (jaFoiImportado(tacoUsuario)) {
                logger.info("Tabela TACO já foi importada anteriormente. Pulando importação.");
                return;
            }

            importarIngredientesSeNecessario(tacoUsuario);
        } catch (Exception e) {
            logger.error("Erro ao inicializar a tabela TACO: {}", e.getMessage(), e);
        }
    }

    private Usuario garantirUsuarioTaco() {
        return usuarioRepository.findByUsernameIgnoreCase(tacoUsername)
                .or(() -> usuarioRepository.findByEmailIgnoreCase(tacoEmail))
                .orElseGet(() -> {
                    logger.info("Usuário TACO não encontrado. Criando usuário padrão...");
                    Usuario usuario = new Usuario();
                    usuario.setUsername(tacoUsername);
                    usuario.setEmail(tacoEmail);
                    usuario.setSenha(passwordEncoder.encode(tacoPassword));
                    usuario.setCargo(Cargo.NUTRICIONISTA);
                    usuario.setAtivo(true);
                    Usuario salvo = usuarioRepository.save(usuario);
                    logger.info("Usuário TACO criado com sucesso.");
                    return salvo;
                });
    }

    private boolean jaFoiImportado(Usuario tacoUsuario) {
        var spec = IngredienteSpecification.filter(null, tacoUsuario.getId(), null, null);
        long countIngredientes = ingredienteRepository.count(spec);
        return countIngredientes > 0;
    }


    private void importarIngredientesSeNecessario(Usuario tacoUsuario) throws Exception {
        var tacoInputStreamOpt = tacoFileReader.abrirArquivo(TACO_CLASSPATH_FILE);
        if (tacoInputStreamOpt.isEmpty()) {
            logger.warn("Arquivo da TACO não encontrado. A tabela TACO não foi importada.");
            return;
        }

        Map<String, BigDecimal> saturadasPorNome = new HashMap<>();
        var acidosInputStreamOpt = tacoFileReader.abrirArquivo(ACIDOS_CLASSPATH_FILE);
        if (acidosInputStreamOpt.isPresent()) {
            try (InputStream inputStream = acidosInputStreamOpt.get()) {
                saturadasPorNome = tacoExcelParser.lerPlanilhaAcidos(inputStream);
            }
            logger.info("Planilha de ácidos carregada. {} alimento(s) com gordura saturada mapeados.", saturadasPorNome.size());
        } else {
            logger.warn("Arquivo de ácidos não encontrado. Gordura saturada ficará sem preenchimento automático.");
        }

        List<TacoIngredienteLinha> linhas;
        try (InputStream inputStream = tacoInputStreamOpt.get()) {
            linhas = tacoExcelParser.lerPlanilhaTaco(inputStream);
        }

        tacoIngredientePersister.persistir(tacoUsuario, linhas, saturadasPorNome);
    }
}
