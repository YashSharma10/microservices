package com.ncu.carbon.carbonservice.controller;

import com.ncu.carbon.carbonservice.dto.CarbonDto;
import com.ncu.carbon.carbonservice.model.Carbon;
import com.ncu.carbon.carbonservice.service.CarbonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/carbon")
public class CarbonController {

    private final CarbonService service;

    public CarbonController(CarbonService service) {
        this.service = service;
    }

    @GetMapping
    public List<CarbonDto> list() {
        try {
            return service.listAll().stream().map(this::toDto).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error listing carbon credits: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarbonDto> get(@PathVariable Long id) {
        try {
            Carbon c = service.get(id);
            if (c == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(toDto(c));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping
    public ResponseEntity<CarbonDto> create(@RequestBody CarbonDto dto) {
        try {
            if (dto.getOwnerId() == null || dto.getPrice() == null) {
                return ResponseEntity.badRequest().build();
            }
            Carbon created = service.create(fromDto(dto));
            return ResponseEntity.created(URI.create("/carbon/" + created.getId())).body(toDto(created));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CarbonDto dto, 
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                       @RequestHeader(value = "X-Service-Request", required = false) String serviceRequest) {
        try {
            // Check ownership before updating (unless it's a service request)
            Carbon existing = service.get(id);
            if (existing == null) return ResponseEntity.notFound().build();
            
            // Allow service-to-service calls to bypass ownership check
            boolean isServiceRequest = "true".equalsIgnoreCase(serviceRequest);
            
            if (!isServiceRequest && (userId == null || !existing.getOwnerId().equals(userId))) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            Carbon c = fromDto(dto);
            c.setId(id);
            if (!isServiceRequest) {
                c.setOwnerId(existing.getOwnerId()); // Preserve owner for user requests
            }
            boolean ok = service.update(c);
            if (!ok) return ResponseEntity.notFound().build();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, 
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        try {
            // Check ownership before deleting
            Carbon existing = service.get(id);
            if (existing == null) return ResponseEntity.notFound().build();
            
            if (userId == null || !existing.getOwnerId().equals(userId)) {
                return ResponseEntity.status(403).build(); // Forbidden
            }
            
            boolean ok = service.delete(id);
            if (!ok) return ResponseEntity.notFound().build();
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private CarbonDto toDto(Carbon c) {
        return new CarbonDto(c.getId(), c.getName(), c.getSupply(), c.getOwnerId(), c.getPrice());
    }

    private Carbon fromDto(CarbonDto d) {
        return new Carbon(d.getId(), d.getName(), d.getSupply(), d.getOwnerId(), d.getPrice());
    }
}
