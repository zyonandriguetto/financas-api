package br.com.unicuritiba.financas.service;

import br.com.unicuritiba.financas.dto.RelatorioMensal;
import br.com.unicuritiba.financas.exception.RecursoNaoEncontradoException;
import br.com.unicuritiba.financas.exception.RegraNegocioException;
import br.com.unicuritiba.financas.model.Movimentacao;
import br.com.unicuritiba.financas.model.TipoMovimentacao;
import br.com.unicuritiba.financas.repository.MovimentacaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Camada de regras de negócio. Todo acesso ao banco é feito por meio do Repository.
 */
@Service
public class MovimentacaoService {

    private final MovimentacaoRepository repository;

    public MovimentacaoService(MovimentacaoRepository repository) {
        this.repository = repository;
    }

    public Movimentacao cadastrar(Movimentacao movimentacao) {
        validar(movimentacao);
        movimentacao.setId(null);
        return repository.save(movimentacao);
    }

    public List<Movimentacao> listar(TipoMovimentacao tipo, String categoria) {
        boolean temCategoria = categoria != null && !categoria.isBlank();
        if (tipo != null && temCategoria) {
            return repository.findByTipoAndCategoriaIgnoreCaseOrderByDataAsc(tipo, categoria);
        }
        if (tipo != null) {
            return repository.findByTipoOrderByDataAsc(tipo);
        }
        if (temCategoria) {
            return repository.findByCategoriaIgnoreCaseOrderByDataAsc(categoria);
        }
        return repository.findAll(Sort.by("data", "id"));
    }

    public Movimentacao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação com id " + id + " não encontrada"));
    }

    public Movimentacao atualizar(Long id, Movimentacao dados) {
        validar(dados);
        Movimentacao existente = buscarPorId(id);
        existente.setDescricao(dados.getDescricao());
        existente.setValor(dados.getValor());
        existente.setTipo(dados.getTipo());
        existente.setData(dados.getData());
        existente.setCategoria(dados.getCategoria());
        return repository.save(existente);
    }

    public void excluir(Long id) {
        Movimentacao existente = buscarPorId(id);
        repository.delete(existente);
    }

    /**
     * Calcula o saldo do mês a partir das movimentações cadastradas:
     * saldo = total de receitas - total de despesas.
     */
    public RelatorioMensal gerarRelatorioMensal(int ano, int mes) {
        if (mes < 1 || mes > 12) {
            throw new RegraNegocioException("O mês deve estar entre 1 e 12");
        }
        YearMonth periodo = YearMonth.of(ano, mes);
        LocalDate inicio = periodo.atDay(1);
        LocalDate fim = periodo.atEndOfMonth();

        BigDecimal receitas = repository.somarPorTipoNoPeriodo(TipoMovimentacao.RECEITA, inicio, fim);
        BigDecimal despesas = repository.somarPorTipoNoPeriodo(TipoMovimentacao.DESPESA, inicio, fim);
        List<Movimentacao> movimentacoes = repository.findByDataBetweenOrderByDataAsc(inicio, fim);

        return new RelatorioMensal(ano, mes, receitas, despesas, receitas.subtract(despesas),
                movimentacoes.size(), movimentacoes);
    }

    /**
     * Regras de negócio (reforçam as validações declaradas na entidade).
     */
    private void validar(Movimentacao m) {
        if (m.getDescricao() == null || m.getDescricao().isBlank()) {
            throw new RegraNegocioException("Toda movimentação deve possuir uma descrição");
        }
        if (m.getValor() == null || m.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor da movimentação deve ser maior que zero");
        }
        if (m.getData() == null) {
            throw new RegraNegocioException("Toda movimentação deve possuir uma data");
        }
        if (m.getTipo() == null) {
            throw new RegraNegocioException("Toda movimentação deve possuir um tipo (RECEITA ou DESPESA)");
        }
    }
}
