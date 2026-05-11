package com.example.sistemanutricao.service.usuario;

import com.example.sistemanutricao.exception.EstabelecimentoNotFoundException;
import com.example.sistemanutricao.exception.DuplicateNomeException;
import com.example.sistemanutricao.exception.UsuarioNotFoundException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.lang.NonNull;

import com.example.sistemanutricao.mapper.UsuarioMapper;
import com.example.sistemanutricao.model.enuns.Cargo;
import com.example.sistemanutricao.model.Estabelecimento;
import com.example.sistemanutricao.model.Usuario;
import com.example.sistemanutricao.record.UsuarioDTO.GetUsuarioDTO;
import com.example.sistemanutricao.record.UsuarioDTO.UsuarioDTO;
import com.example.sistemanutricao.repository.EstabelecimentoRepository;
import com.example.sistemanutricao.repository.UsuarioRepository;
import com.example.sistemanutricao.service.port.ImageStorage;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    private final UsuarioRepository usuarioRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorage imageStorage;
    private final UsuarioMapper usuarioMapper;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EstabelecimentoRepository estabelecimentoRepository,
                          PasswordEncoder passwordEncoder,
                          ImageStorage imageStorage,
                          UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.estabelecimentoRepository = estabelecimentoRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageStorage = imageStorage;
        this.usuarioMapper = usuarioMapper;
    }

    // PERFIL

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "usuarios", allEntries = true)
    public GetUsuarioDTO atualizarPerfilComImagem(@NonNull Long id, UsuarioDTO dto, MultipartFile arquivoImagem) {
        logger.info("Iniciando atualização de perfil com imagem para usuário ID: {}", id);
        Usuario usuario = findUsuarioById(id);

        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());

        if (dto.novaSenha() != null && !dto.novaSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }

        if (arquivoImagem != null && !arquivoImagem.isEmpty()) {
            if (usuario.getCaminhoImagem() != null && !usuario.getCaminhoImagem().isEmpty()) {
                imageStorage.removerImagemPerfil(usuario.getCaminhoImagem());
            }
            String caminhoImagem = imageStorage.armazenarImagemPerfil(arquivoImagem, usuario.getUsername());
            usuario.setCaminhoImagem(caminhoImagem);
        }

        Usuario updated = usuarioRepository.save(usuario);
        logger.info("Usuário salvo com sucesso: ID={}", updated.getId());
        return usuarioMapper.toGetDTO(updated);
    }

    public Resource obterImagemPerfil(@NonNull Long usuarioId) {
        Usuario usuario = findUsuarioById(usuarioId);

        if (usuario.getCaminhoImagem() == null || usuario.getCaminhoImagem().isEmpty()) {
            throw new UsuarioNotFoundException("Usuário não possui imagem de perfil");
        }

        return imageStorage.carregarImagem(usuario.getCaminhoImagem());
    }

    // CRUD

    @Transactional(rollbackFor = Exception.class)
    public Usuario create(UsuarioDTO registroDto) {
        if (usuarioRepository.findByEmail(registroDto.email()).isPresent()) {
            throw new DuplicateNomeException("Este e-mail já está em uso. Tente fazer login.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(registroDto.username());
        usuario.setEmail(registroDto.email());
        usuario.setSenha(passwordEncoder.encode(registroDto.senha()));
        usuario.setAtivo(false);

        return usuarioRepository.save(usuario);
    }

    @Transactional(rollbackFor = Exception.class)
    public void inicializarAdminPadrao() {
        var usuariosAdmin = usuarioRepository.findByCargo(Cargo.ADMIN);
        
        if (usuariosAdmin.isEmpty()) {
            logger.info("Nenhum usuário admin encontrado. Criando usuário admin padrão...");
            try {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setEmail("admin@gmail.com");
                admin.setSenha(passwordEncoder.encode("1234567"));
                admin.setCargo(Cargo.ADMIN);
                admin.setAtivo(true);
                
                usuarioRepository.save(admin);
                logger.info("Usuário admin padrão criado com sucesso!");
                logger.info("Username: admin");
                logger.info("Email: admin@gmail.com");
                logger.info("Senha: 1234567");
            } catch (Exception e) {
                logger.error("Erro ao criar usuário admin padrão: {}", e.getMessage(), e);
            }
        } else {
            logger.info("Usuário(s) admin já existente(s) no sistema. Nenhuma ação necessária.");
        }
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void atualizarEstabelecimento(@NonNull Long usuarioId, @NonNull Long estabelecimentoId) {
        Usuario usuario = findUsuarioById(usuarioId);

        Estabelecimento estabelecimento = estabelecimentoRepository.findById(estabelecimentoId)
                .orElseThrow(() -> new EstabelecimentoNotFoundException("Estabelecimento não encontrado"));

        usuario.setEstabelecimento(estabelecimento);
        usuarioRepository.save(usuario);
    }

    public List<GetUsuarioDTO> listAll() {
        return listPage(-1L, PageRequest.of(0, 500)).getContent();
    }

    public Page<GetUsuarioDTO> listPage(Pageable pageable) {
        return listPage(-1L, pageable);
    }

    public Page<GetUsuarioDTO> listPage(@NonNull Long adminId, Pageable pageable) {
        return usuarioRepository.findAdministradoresComuns(adminId, "TACO", pageable)
                .map(usuarioMapper::toGetDTO);
    }

    public GetUsuarioDTO findById(@NonNull Long id) {
        return usuarioMapper.toGetDTO(findUsuarioById(id));
    }

    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "usuarios", allEntries = true)
    public GetUsuarioDTO update(@NonNull Long id, UsuarioDTO dto) {
        logger.info("Iniciando atualização para usuário ID: {}", id);
        Usuario usuario = findUsuarioById(id);

        usuario.setUsername(dto.username());
        usuario.setEmail(dto.email());

        if (dto.novaSenha() != null && !dto.novaSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        }

        if (dto.caminhoImagem() != null && !dto.caminhoImagem().isEmpty()) {
            usuario.setCaminhoImagem(dto.caminhoImagem());
        }

        Usuario updated = usuarioRepository.save(usuario);
        logger.info("Usuário salvo com sucesso: ID={}", updated.getId());
        return usuarioMapper.toGetDTO(updated);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public void updateCargo(@NonNull Long id, Cargo novoCargo) {
        Usuario usuario = findUsuarioById(id);
        usuario.setCargo(novoCargo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void toggleAtivo(@NonNull Long id) {
        Usuario usuario = findUsuarioById(id);
        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);
    }

    // HELPERS

    private Usuario findUsuarioById(@NonNull Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException("Usuário não encontrado"));
    }
}