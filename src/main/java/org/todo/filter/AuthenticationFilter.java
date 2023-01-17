package org.todo.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.todo.model.user.User;
import org.todo.model.user.UserAdmin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebFilter(urlPatterns = "/api/todos/*")
public class AuthenticationFilter extends HttpFilter {

    private static final UserAdmin userAdmin = UserAdmin.getInstance() ;

	public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
	   try {
           String[] credentials = getCredentials(request);
           User user = validateCredentials(credentials);
           request.setAttribute("user", user);
       } catch (Exception ex) {
           response.setStatus(401); return;
       }
       chain.doFilter(request, response);
   }

   private String[] getCredentials(HttpServletRequest request) throws Exception {
       String authHeader = request.getHeader("Authorization");
       String[] tokens = authHeader.split(" ");
       if (!tokens[0].equals("Basic")) throw new Exception();
       byte[] decoded = Base64.getDecoder().decode(tokens[1]);
       return new String(decoded, StandardCharsets.UTF_8).split(":");
   }

   private User validateCredentials(String[] credentials) throws Exception {

       User user = userAdmin.loginUser(credentials[0], credentials[1]);
       return user;
   }
}
