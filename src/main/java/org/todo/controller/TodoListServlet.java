package org.todo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.todo.model.todo.Todo;
import org.todo.model.todo.TodoList;
import org.todo.model.todo.TodoNotFoundException;
import org.todo.model.user.User;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/todos/*")
public class TodoListServlet extends HttpServlet {


    private static final ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        User user = (User) request.getAttribute("user");
        TodoList todoList = user.getTodoList();


        if (pathInfo == null) {
            // Todo: filter? using category as query?
            String category = request.getParameter("category");

            //either returns all todos or todos specified by given category
            List<Todo> todos = category == null ? todoList.getTodos() : todoList.getTodos(category);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getOutputStream(), todos);

        }else { // returns todo with the given specified id
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Todo todo = todoList.findTodo(id);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                objectMapper.writeValue(response.getOutputStream(), todo);
            } catch (NumberFormatException | TodoNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            //a method getInputStream() is used to get all parameters of Todo
            Todo todo = objectMapper.readValue(request.getInputStream(), Todo.class);
            User user = (User) request.getAttribute("user");
            TodoList todoList = user.getTodoList();

            // Location Header
            response.setHeader("Location", request.getRequestURI() + "/" + todo.getId());
            // return created todo
            response.setContentType("application/json");
            todoList.addTodo(todo);
            response.setStatus(HttpServletResponse.SC_CREATED);
        }catch (JsonProcessingException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        User user = (User) request.getAttribute("user");
        TodoList todoList = user.getTodoList();
        if (pathInfo != null) {
            try {

                //read the requested body using getInputStream() method
                Todo todo =  objectMapper.readValue(request.getInputStream(), Todo.class);

                 //get the identifier
                 int id = Integer.parseInt(pathInfo.substring(1));
                 if (todo.getId() != id || todo.getTitle() == null || todo.getTitle().isEmpty()){
                     response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                 }

                 todoList.updateTodo(todo);

            } catch (TodoNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        User user = (User) request.getAttribute("user");

        TodoList todoList = user.getTodoList();
        if (pathInfo != null) {
            int id = Integer.parseInt(pathInfo.substring(1));
            try {
                todoList.removeTodo(id);
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (TodoNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

        }
    }
}
