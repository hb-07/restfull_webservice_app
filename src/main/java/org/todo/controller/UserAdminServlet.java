package org.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.todo.model.user.User;
import org.todo.model.user.UserAdmin;
import org.todo.model.user.UserAlreadyExistsException;

import java.io.IOException;

@WebServlet("/api/users/*")
public class UserAdminServlet extends HttpServlet {
    private static final ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        User user = objectMapper.readValue(req.getInputStream(), User.class);
        UserAdmin userAdmin = UserAdmin.getInstance();

        try {
            if (user != null ) {
                userAdmin.registerUser(user.getName(), user.getPassword());
                resp.setStatus(HttpServletResponse.SC_CREATED);
            }else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }catch (UserAlreadyExistsException ex) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        }catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        }

    }
}
