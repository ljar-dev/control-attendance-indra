package com.indra.attendance_control.jwt;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.indra.attendance_control.models.Employee;


@Component
public class JwtUtil {


    private final String SECRET = "mysecretkey001"; // Clave secreta para firmar el token

    public String generateToken(String username) {
        // Aquí iría la lógica para generar un token JWT usando la biblioteca que prefieras
        // Por simplicidad, retornamos un token simulado

        return JWT.create()
            .withSubject(username)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 24)) // 24 hora de validez
            .sign(Algorithm.HMAC256(SECRET));
    }

    public String generateToken(String username, List<String> roles) {
        // Aquí iría la lógica para generar un token JWT usando la biblioteca que prefieras
        // Por simplicidad, retornamos un token simulado

        return JWT.create()
            .withSubject(username)
            .withClaim("roles", roles)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 24)) // 24 hora de validez
            .sign(Algorithm.HMAC256(SECRET));
    }

    /**
     *  NUEVO: Genera un token JWT completo con datos del empleado
     */
    public String generateToken(String username, List<String> roles, Employee employee) {
        return JWT.create()
            .withSubject(username)
            .withClaim("roles", roles)
            .withClaim("employeeId", employee.getIdEmployee())
            .withClaim("employeeCode", employee.getEmployeeCode())
            .withClaim("firstName", employee.getFirstName())
            .withClaim("lastName", employee.getLastName())
            .withClaim("department", employee.getDepartment())
            .withClaim("position", employee.getPosition())
            .withClaim("mustChangePassword", employee.getUser().isMustChangePassword())
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 24))
            .sign(Algorithm.HMAC256(SECRET));
    }


    public boolean validateToken(String token, String username) {
        // Aquí iría la lógica para validar el token JWT

        DecodedJWT jwt = getDecodedJWT(token);
        return  jwt.getSubject().equals(username) && jwt.getExpiresAt().after(new Date());

    }

    // Obtener usuario del token
    public String getUsername(String token) {
        return getDecodedJWT(token).getSubject();
    }    

    private DecodedJWT getDecodedJWT(String token) {

        
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        return verifier.verify(token);
            

    }    

    public List<String> getRoles(String token) {
        DecodedJWT jwt = getDecodedJWT(token);
        return jwt.getClaim("roles").asList(String.class);
    }    

    /**
     *  NUEVO: Extrae el employeeId del token
     */
    public Long extractEmployeeId(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET))
            .build()
            .verify(token)
            .getClaim("employeeId")
            .asLong();
    }
}
