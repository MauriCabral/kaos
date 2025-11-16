package org.example.kaos.service;

import org.example.kaos.entity.Extra;

public interface IExtraService {
    Extra getSingleExtra();
    boolean hasExtra();
    void saveExtra(Extra extra);
}
