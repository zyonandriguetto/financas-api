package br.com.unicuritiba.financas.controller;

import br.com.unicuritiba.financas.dto.RelatorioMensal;
import br.com.unicuritiba.financas.model.Movimentacao;
import br.com.unicuritiba.financas.model.TipoMovimentacao;
import br.com.unicuritiba.financas.service.MovimentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService service;

    public MovimentacaoController(MovimentacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Movimentacao> cadastrar(@Valid @RequestBody Movimentacao movimentacao) {
        Movimentacao salva = service.cadastrar(movimentacao);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(salva.getId()).toUri();
        return ResponseEntity.created(location).body(salva);
    }

    @GetMapping
    public List<Movimentacao> listar(@RequestParam(required = false) TipoMovimentacao tipo,
                                     @RequestParam(required = false) String categoria) {
        return service.listar(tipo, categoria);
    }

    @GetMapping("/{id}")
    public Movimentacao buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}")
    public Movimentacao atualizar(@PathVariable Long id, @Valid @RequestBody Movimentacao movimentacao) {
        return service.atualizar(id, movimentacao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/relatorio")
    public RelatorioMensal relatorioMensal(@RequestParam int ano, @RequestParam int mes) {
        return service.gerarRelatorioMensal(ano, mes);
    }
}
