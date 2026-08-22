package gt.com.ro.devappumg.models;

public class UserResponse {
    private int id;
    private String username;
    private String email;
    private String nombre;
    private String apellido;
    private boolean activo;
    private String fechaCreacion;
    private String fechaActualizacion;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public boolean isActivo() { return activo; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaActualizacion() { return fechaActualizacion; }
}