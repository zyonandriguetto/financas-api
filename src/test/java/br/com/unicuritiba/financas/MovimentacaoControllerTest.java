package br.com.unicuritiba.financas;

import br.com.unicuritiba.financas.repository.MovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MovimentacaoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MovimentacaoRepository repository;

    @BeforeEach
    void limpar() {
        repository.deleteAll();
    }

    private String json(String descricao, String valor, String tipo, String data, String categoria) {
        return """
                {"descricao": "%s", "valor": %s, "tipo": "%s", "data": "%s", "categoria": "%s"}
                """.formatted(descricao, valor, tipo, data, categoria);
    }

    private void cadastrar(String corpo) throws Exception {
        mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated());
    }

    @Test
    void deveExecutarCrudCompleto() throws Exception {
        String resposta = mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Salário", "5000.00", "RECEITA", "2026-09-01", "Trabalho")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("RECEITA"))
                .andReturn().getResponse().getContentAsString();
        String id = resposta.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/movimentacoes")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/movimentacoes/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Salário"));

        mvc.perform(put("/movimentacoes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Salário + bônus", "5500.00", "RECEITA", "2026-09-01", "Trabalho")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(5500.00));

        mvc.perform(delete("/movimentacoes/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/movimentacoes/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarMovimentacaoInvalida() throws Exception {
        mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teste", "0", "DESPESA", "2026-09-01", "X")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "10", "DESPESA", "2026-09-01", "X")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teste", "10", "INVESTIMENTO", "2026-09-01", "X")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/movimentacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"Teste\", \"valor\": 10, \"tipo\": \"DESPESA\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveCalcularSaldoMensal() throws Exception {
        cadastrar(json("Salário", "5000.00", "RECEITA", "2026-09-01", "Trabalho"));
        cadastrar(json("Aluguel", "1500.00", "DESPESA", "2026-09-05", "Moradia"));
        cadastrar(json("Mercado", "800.50", "DESPESA", "2026-09-10", "Alimentação"));
        cadastrar(json("Freela", "999.00", "RECEITA", "2026-10-02", "Trabalho"));

        mvc.perform(get("/movimentacoes/relatorio").param("ano", "2026").param("mes", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(5000.00))
                .andExpect(jsonPath("$.totalDespesas").value(2300.50))
                .andExpect(jsonPath("$.saldo").value(2699.50))
                .andExpect(jsonPath("$.quantidadeMovimentacoes").value(3));

        mvc.perform(get("/movimentacoes").param("tipo", "DESPESA"))
                .andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/movimentacoes").param("categoria", "trabalho"))
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
