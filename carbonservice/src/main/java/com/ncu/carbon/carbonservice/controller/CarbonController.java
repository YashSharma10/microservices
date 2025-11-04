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
        Carbon created = service.create(fromDto(dto));
        return ResponseEntity.created(URI.create("/carbon/" + created.getId())).body(toDto(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody CarbonDto dto) {
        Carbon c = fromDto(dto);
        c.setId(id);
        boolean ok = service.update(c);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean ok = service.delete(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    private CarbonDto toDto(Carbon c) {
        return new CarbonDto(c.getId(), c.getName(), c.getSupply());
    }

    private Carbon fromDto(CarbonDto d) {
        return new Carbon(d.getId(), d.getName(), d.getSupply());
    }
}
