package ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class WidgetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysExpect(status().isOk())
                .alwaysExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .build();
    }

    @Test
    public void putWidget() throws Exception {
        this.mvc.perform(post("/api/widgets")
                .accept(MediaType.APPLICATION_JSON)
                .content("x=1&y=2&width=3&height=4&zIndex=5")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.x", is(1)))
                .andExpect(jsonPath("$.y", is(2)))
                .andExpect(jsonPath("$.width", is(3)))
                .andExpect(jsonPath("$.height", is(4)))
                .andExpect(jsonPath("$.zIndex", is(5)));
    }

    @Test
    public void updateWidget() throws Exception {
        MvcResult result1 = this.mvc.perform(post("/api/widgets")
                .accept(MediaType.APPLICATION_JSON)
                .content("x=0&y=0&width=1&height=1&zIndex=1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andReturn();

        Widget widget1 = getWidgetFromResult(result1);

        this.mvc.perform(post("/api/widgets/" + widget1.getId().toString())
                .accept(MediaType.APPLICATION_JSON)
                .content("x=1&y=2&width=3&height=4&zIndex=5")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(jsonPath("$.id", is(widget1.getId().toString())))
                .andExpect(jsonPath("$.x", is(1)))
                .andExpect(jsonPath("$.y", is(2)))
                .andExpect(jsonPath("$.width", is(3)))
                .andExpect(jsonPath("$.height", is(4)))
                .andExpect(jsonPath("$.zIndex", is(5)));

        MockMvc mvc2 = MockMvcBuilders.webAppContextSetup(this.wac)
//                .alwaysExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .build();

        mvc2.perform(post("/api/widgets/" + widget1.getId().toString())
                .accept(MediaType.APPLICATION_JSON)
                .content("x=0&y=0&width=-1&height=1&zIndex=1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getAllWidgets() throws Exception {
        this.mvc.perform(get("/api/widgets").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    public void checkWidgetOrder() throws Exception {
        MvcResult result1 = this.mvc.perform(post("/api/widgets")
                .accept(MediaType.APPLICATION_JSON)
                .content("x=0&y=0&width=1&height=1&zIndex=1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(jsonPath("$.zIndex", is(1)))
                .andReturn();

        MvcResult result2 = this.mvc.perform(post("/api/widgets")
                .accept(MediaType.APPLICATION_JSON)
                .content("x=1&y=1&width=1&height=1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(jsonPath("$.zIndex", is(2)))
                .andReturn();

        MvcResult result3 = this.mvc.perform(post("/api/widgets")
                .accept(MediaType.APPLICATION_JSON)
                .content("x=2&y=2&width=1&height=1&zIndex=1")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        )
                .andExpect(jsonPath("$.zIndex", is(1)))
                .andReturn();

        Widget widget1 = getWidgetFromResult(result1);
        Widget widget2 = getWidgetFromResult(result2);
        Widget widget3 = getWidgetFromResult(result3);

        this.mvc.perform(get("/api/widgets").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(widget3.getId().toString())))
                .andExpect(jsonPath("$[1].id", is(widget1.getId().toString())))
                .andExpect(jsonPath("$[2].id", is(widget2.getId().toString())));
    }

    private Widget getWidgetFromResult(MvcResult result) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(result.getResponse().getContentAsString(), Widget.class);
    }

}
