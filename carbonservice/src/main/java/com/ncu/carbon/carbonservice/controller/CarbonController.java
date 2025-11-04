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
        return service.listAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarbonDto> get(@PathVariable Long id) {
        Carbon c = service.get(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDto(c));
    }

    @PostMapping
    public ResponseEntity<CarbonDto> create(@RequestBody CarbonDto dto) {
        if (dto.getOwnerId() == null || dto.getPrice() == null) {
            return ResponseEntity.badRequest().build();
        }
        Carbon created = service.create(fromDto(dto));
        return ResponseEntity.created(URI.create("/carbon/" + created.getId())).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CarbonDto dto, 
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // Check ownership before updating
        Carbon existing = service.get(id);
        if (existing == null) return ResponseEntity.notFound().build();
        
        if (userId == null || !existing.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        Carbon c = fromDto(dto);
        c.setId(id);
        c.setOwnerId(existing.getOwnerId()); // Preserve owner
        boolean ok = service.update(c);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, 
                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        // Check ownership before deleting
        Carbon existing = service.get(id);
        if (existing == null) return ResponseEntity.notFound().build();
        
        if (userId == null || !existing.getOwnerId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        
        boolean ok = service.delete(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    private CarbonDto toDto(Carbon c) {
        return new CarbonDto(c.getId(), c.getName(), c.getSupply(), c.getOwnerId(), c.getPrice());
    }

    private Carbon fromDto(CarbonDto d) {
        return new Carbon(d.getId(), d.getName(), d.getSupply(), d.getOwnerId(), d.getPrice());
    }
}
