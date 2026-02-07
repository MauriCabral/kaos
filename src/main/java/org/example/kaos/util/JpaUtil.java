package org.example.kaos.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaUtil {
    private static final Logger logger = LoggerFactory.getLogger(JpaUtil.class);

    private static final EntityManagerFactory emf;

    static {
        try {
            logger.info("Inicializando EntityManagerFactory para persistencia 'kaos'...");
            emf = Persistence.createEntityManagerFactory("kaos");
            logger.info("JPA inicializado correctamente. Conexión a la base de datos exitosa.");
        } catch (Exception ex) {
            logger.error("Error inicializando JPA: {}", ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        logger.debug("Solicitando EntityManagerFactory");
        return emf;
    }

    public static EntityManager getEntityManager() {
        logger.debug("Creando nuevo EntityManager");
        return emf.createEntityManager();
    }
}