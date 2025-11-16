package org.example.kaos.service.implementation;

import org.example.kaos.entity.Extra;
import org.example.kaos.repository.ExtraRepository;
import org.example.kaos.service.IExtraService;

import java.util.List;

public class ExtraServiceImpl implements IExtraService {
    private final ExtraRepository extraRepository;

    public ExtraServiceImpl() {
        this.extraRepository = new ExtraRepository();
    }

    @Override
    public Extra getSingleExtra() {
        List<Extra> extras = extraRepository.findAll();
        return extras.isEmpty() ? null : extras.get(0);
    }

    @Override
    public boolean hasExtra() {
        return getSingleExtra() != null;
    }

    @Override
    public void saveExtra(Extra extra) {
        extraRepository.save(extra);
    }
}
