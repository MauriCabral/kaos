package org.example.kaos.service.implementation;

import org.example.kaos.entity.VariantType;
import org.example.kaos.repository.VariantRepository;
import org.example.kaos.service.IVariantService;

import java.util.List;

public class VariantServiceImpl implements IVariantService {
    private final VariantRepository variantRepository = new VariantRepository();

    @Override
    public List<VariantType> getAllVariants() {
        List<VariantType> variant = variantRepository.findAll();
        if (variant != null) {
            return variant;
        }
        return null;
    }
}
