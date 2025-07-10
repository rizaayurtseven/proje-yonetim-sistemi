package com.stajprojesi.backend.controller;

import com.stajprojesi.backend.model.Project;
import com.stajprojesi.backend.repository.ProjectRepository;
import com.stajprojesi.backend.model.Employee;
import com.stajprojesi.backend.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.stajprojesi.backend.model.ProjectFile;
import com.stajprojesi.backend.repository.ProjectFileRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.hibernate.Hibernate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import com.stajprojesi.backend.model.User;
import com.stajprojesi.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectFileRepository projectFileRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        try {
            List<Project> projects = projectRepository.findAll();
            // Dosyaları da yükle (Lazy fetch için)
            for (Project p : projects) {
                Hibernate.initialize(p.getFiles());
            }
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        try {
            Optional<Project> project = projectRepository.findById(id);
            if (project.isPresent()) {
                return ResponseEntity.ok(project.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try {
            // Basit validation
            if (project.getName() == null || project.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            // Giriş yapan kullanıcıyı owner olarak ata
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User owner = userRepository.findByUsername(username).orElse(null);
            if (owner != null) {
                project.setOwner(owner);
            }
            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project updatedProject) {
        try {
            Optional<Project> optionalProject = projectRepository.findById(id);
            if (!optionalProject.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Project project = optionalProject.get();
            project.setName(updatedProject.getName());
            project.setDescription(updatedProject.getDescription());
            project.setStatus(updatedProject.getStatus());
            
            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            if (!projectRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            // Giriş yapan kullanıcıyı al
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) return ResponseEntity.status(403).build();
            Project project = projectRepository.findById(id).orElse(null);
            if (project == null) return ResponseEntity.notFound().build();
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));
            boolean isOwner = project.getOwner() != null && project.getOwner().getUsername().equals(username);
            if (isAdmin || isOwner) {
                projectRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(403).build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{projectId}/assign/{employeeId}")
    public ResponseEntity<Project> assignEmployeeToProject(@PathVariable Long projectId, @PathVariable Long employeeId) {
        try {
            Optional<Project> optionalProject = projectRepository.findById(projectId);
            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
            
            if (!optionalProject.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Project project = optionalProject.get();
            Employee employee = optionalEmployee.get();
            
            project.getEmployees().add(employee);
            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{projectId}/unassign/{employeeId}")
    public ResponseEntity<Project> unassignEmployeeFromProject(@PathVariable Long projectId, @PathVariable Long employeeId) {
        try {
            Optional<Project> optionalProject = projectRepository.findById(projectId);
            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
            
            if (!optionalProject.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            if (!optionalEmployee.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Project project = optionalProject.get();
            Employee employee = optionalEmployee.get();
            
            project.getEmployees().remove(employee);
            Project savedProject = projectRepository.save(project);
            return ResponseEntity.ok(savedProject);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{projectId}/employees")
    public ResponseEntity<Set<Employee>> getEmployeesOfProject(@PathVariable Long projectId) {
        try {
            Optional<Project> optionalProject = projectRepository.findById(projectId);
            if (!optionalProject.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            Project project = optionalProject.get();
            return ResponseEntity.ok(project.getEmployees());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{projectId}/uploadFile")
    public ResponseEntity<?> uploadFile(@PathVariable Long projectId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 10_000_000) return ResponseEntity.badRequest().body("Dosya çok büyük!");
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            Project project = projectRepository.findById(projectId).orElseThrow();
            ProjectFile projectFile = new ProjectFile();
            projectFile.setFileName(file.getOriginalFilename());
            projectFile.setFileType(file.getContentType());
            projectFile.setFilePath(filePath.toString());
            projectFile.setProject(project);
            projectFileRepository.save(projectFile);
            return ResponseEntity.ok("Dosya yüklendi");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Dosya yüklenemedi");
        }
    }
}
