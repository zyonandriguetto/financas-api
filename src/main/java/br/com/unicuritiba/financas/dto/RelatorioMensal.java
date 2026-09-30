package br.com.unicuritiba.financas.dto;

import br.com.unicuritiba.financas.model.Movimentacao;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resposta do relatório mensal. O saldo é calculado em tempo de execução
 * (total de receitas - total de despesas) e nunca é persistido.
 */
public record RelatorioMensal(
        int ano,
        int mes,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        int quantidadeMovimentacoes,
        List<Movimentacao> movimentacoes
) {
}
