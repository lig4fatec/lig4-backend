package br.com.lig4.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class JogadorResourceTest {

    @Test
    void deveCriarEBuscarJogador() {
        String corpo = """
                {
                    "nome": "Ana Silva",
                    "email": "ana.silva@lig4.com"
                }
                """;

        Integer id = given()
                .contentType("application/json")
                .body(corpo)
                .when().post("/jogadores")
                .then()
                .statusCode(201)
                .body("nome", is("Ana Silva"))
                .body("id", notNullValue())
                .extract().path("id");

        given()
                .when().get("/jogadores/" + id)
                .then()
                .statusCode(200)
                .body("email", is("ana.silva@lig4.com"));
    }

    @Test
    void naoDeveCriarJogadorComEmailDuplicado() {
        String corpo = """
                {
                    "nome": "Bruno Costa",
                    "email": "bruno.costa@lig4.com"
                }
                """;

        given().contentType("application/json").body(corpo)
                .when().post("/jogadores")
                .then().statusCode(201);

        given().contentType("application/json").body(corpo)
                .when().post("/jogadores")
                .then().statusCode(409);
    }

    @Test
    void deveRetornar404ParaJogadorInexistente() {
        given()
                .when().get("/jogadores/999999")
                .then()
                .statusCode(404);
    }
}
