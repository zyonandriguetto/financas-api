package br.com.unicuritiba.financas.repository;

import br.com.unicuritiba.financas.model.Movimentacao;
import br.com.unicuritiba.financas.model.TipoMovimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByTipoOrderByDataAsc(TipoMovimentacao tipo);

    List<Movimentacao> findByCategoriaIgnoreCaseOrderByDataAsc(String categoria);

    List<Movimentacao> findByTipoAndCategoriaIgnoreCaseOrderByDataAsc(TipoMovimentacao tipo, String categoria);

    List<Movimentacao> findByDataBetweenOrderByDataAsc(LocalDate inicio, LocalDate fim);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM Movimentacao m "
            + "WHERE m.tipo = :tipo AND m.data BETWEEN :inicio AND :fim")
    BigDecimal somarPorTipoNoPeriodo(@Param("tipo") TipoMovimentacao tipo,
                                     @Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim);
}
