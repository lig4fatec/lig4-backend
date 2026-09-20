package br.com.lig4.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class PartidaResourceTest {

    private Integer criarJogador(String nome, String email) {
        String corpo = """
                {"nome": "%s", "email": "%s"}
                """.formatted(nome, email);

        return given()
                .contentType("application/json")
                .body(corpo)
                .when().post("/jogadores")
                .then().statusCode(201)
                .extract().path("id");
    }

    @Test
    void deveCriarPartidaEFinalizarComVencedor() {
        Integer jogador1 = criarJogador("Carla", "carla@lig4.com");
        Integer jogador2 = criarJogador("Diego", "diego@lig4.com");

        String corpoPartida = """
                {"jogadorUmId": %d, "jogadorDoisId": %d}
                """.formatted(jogador1, jogador2);

        Integer partidaId = given()
                .contentType("application/json")
                .body(corpoPartida)
                .when().post("/partidas")
                .then().statusCode(201)
                .body("status", is("EM_ANDAMENTO"))
                .extract().path("id");

        String corpoFinalizar = """
                {"vencedorId": %d}
                """.formatted(jogador1);

        given()
                .contentType("application/json")
                .body(corpoFinalizar)
                .when().put("/partidas/" + partidaId)
                .then().statusCode(200)
                .body("status", is("FINALIZADA"))
                .body("vencedor.id", is(jogador1))
                .body("finalizadoEm", notNullValue());
    }

    @Test
    void deveRegistrarEmpateQuandoVencedorIdForNulo() {
        Integer jogador1 = criarJogador("Elis", "elis@lig4.com");
        Integer jogador2 = criarJogador("Fabio", "fabio@lig4.com");

        String corpoPartida = """
                {"jogadorUmId": %d, "jogadorDoisId": %d}
                """.formatted(jogador1, jogador2);

        Integer partidaId = given()
                .contentType("application/json")
                .body(corpoPartida)
                .when().post("/partidas")
                .then().statusCode(201)
                .extract().path("id");

        given()
                .contentType("application/json")
                .body("{}")
                .when().put("/partidas/" + partidaId)
                .then().statusCode(200)
                .body("status", is("FINALIZADA"))
                .body("vencedor", nullValue());
    }

    @Test
    void naoDeveCriarPartidaComJogadorIgual() {
        Integer jogador1 = criarJogador("Gustavo", "gustavo@lig4.com");

        String corpoPartida = """
                {"jogadorUmId": %d, "jogadorDoisId": %d}
                """.formatted(jogador1, jogador1);

        given()
                .contentType("application/json")
                .body(corpoPartida)
                .when().post("/partidas")
                .then().statusCode(400);
    }

    @Test
    void naoDeveCriarPartidaComJogadorInexistente() {
        Integer jogador1 = criarJogador("Helena", "helena@lig4.com");

        String corpoPartida = """
                {"jogadorUmId": %d, "jogadorDoisId": 999999}
                """.formatted(jogador1);

        given()
                .contentType("application/json")
                .body(corpoPartida)
                .when().post("/partidas")
                .then().statusCode(404);
    }
}
