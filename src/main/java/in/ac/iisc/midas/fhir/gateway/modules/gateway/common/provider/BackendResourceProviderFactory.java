package in.ac.iisc.midas.fhir.gateway.modules.gateway.common.provider;

import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import in.ac.iisc.midas.fhir.gateway.modules.gateway.common.target.TargetRequestForwarder;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class BackendResourceProviderFactory extends ResourceProviderFactory {
    private final TargetRequestForwarder requestForwarder;

    public BackendResourceProviderFactory(TargetRequestForwarder requestForwarder) {
        this.requestForwarder = requestForwarder;
        connect();
        addSupplier(() -> new BackendSystemProvider(requestForwarder));
    }

    private void connect() {
        getClassesOfTypeInPackage("org.hl7.fhir.r4.model", Resource.class)
                .forEach(klass -> addSupplier(() -> new BackendResourceProvider<>(requestForwarder, klass)));
    }

    /**
     * Retrieves all class references in a given package that are of a specific subtype.
     *
     * @param packageName the name of the package to scan
     * @param superType   the class type to filter by
     * @param <T>         the type of the class to filter by
     * @return a set of class references that are of the specified subtype
     */
    private static <T> Set<Class<? extends T>> getClassesOfTypeInPackage(String packageName, Class<T> superType) {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);

        // Add a filter to include only classes that are assignable from the specified superType
        TypeFilter filter = new AssignableTypeFilter(superType);
        scanner.addIncludeFilter(filter);

        Set<Class<? extends T>> classes = new HashSet<>();

        // Scan the specified package
        scanner.findCandidateComponents(packageName).forEach(beanDefinition -> {
            try {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                if (superType.isAssignableFrom(clazz) && !superType.equals(clazz)) {
                    classes.add(clazz.asSubclass(superType));
                }
            } catch (ClassNotFoundException e) {
                log.info(String.format("Unable to load resource class for  %s", beanDefinition.getBeanClassName()));
            }
        });

        return classes;
    }
}
