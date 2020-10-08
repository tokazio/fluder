package fr.tokazio.fluder.processor;

import fr.tokazio.fluder.annotations.Buildable;

import java.util.*;

import static fr.tokazio.fluder.processor.FluderCandidate.firstLower;
import static fr.tokazio.fluder.processor.FluderCandidate.firstUpper;

/**
 * https://dzone.com/articles/fluent-builder-pattern
 */
public class Fluder {

    private Buildable buildable;
    private final List<FluderCandidate> requiredCandidates = new LinkedList<>();
    private final List<FluderCandidate> optionalCandidates = new LinkedList<>();
    private final List<FluderFile> files = new LinkedList<>();

    public List<FluderFile> generate(final Buildable buildable, final String packageName, final String simpleClassName, final boolean noArgCtorIsNonPublic, final List<FluderCandidate> candidatesOrg) {
        this.buildable = buildable;
        orderCandidates(candidatesOrg);
        sortRequiredAndOptionalCandidates(candidatesOrg);
        generateRequiredInterfaces(packageName, simpleClassName);
        generateCreatorInterface(packageName, simpleClassName);
        generateBuilderClass(packageName, simpleClassName, noArgCtorIsNonPublic);
        return files;
    }

    private void generateBuilderClass(final String packageName, final String simpleClassName, final boolean noArgCtorIsNonPublic) {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        final String builderName = builderName(simpleClassName);
        sb.append("public class ").append(builderName).append(" implements ");
        generateBuilderImplements(sb, simpleClassName);
        sb.append("{\n\n");
        generateBuilderFields(sb);
        generateBuilderCtor(sb, simpleClassName);
        generateBuilderSettersImplementations(sb, simpleClassName);
        generateBuilderOptionalSettersImplementations(sb, simpleClassName);
        generateBuilderBuildMethod(sb, simpleClassName, noArgCtorIsNonPublic);
        generateBuilderSingleton(sb, builderName);
        sb.append("\n}");
        files.add(new FluderFile(builderName, sb.toString()));
    }

    private String builderName(final String simpleClassName) {
        if (buildable.builderName().isEmpty()) {
            return simpleClassName + "Builder";
        }
        if (buildable.builderName().contains("$")) {
            return firstUpper(buildable.builderName()).replace("$", simpleClassName);
        }
        return firstUpper(buildable.builderName());
    }

    private String creatorName(final String simpleClassName) {
        if (buildable.creatorName().isEmpty()) {
            return simpleClassName + "Creator";
        }
        if (buildable.creatorName().contains("$")) {
            return firstUpper(buildable.creatorName()).replace("$", simpleClassName);
        }
        return firstUpper(buildable.creatorName());
    }

    private void generateBuilderSingleton(final StringBuilder sb, final String builderName) {
        sb.append("\tpublic static ").append(requiredCandidates.get(0).intfName()).append(" ").append(instanceMethodName()).append("(){\n")
                .append("\t\treturn new ").append(builderName).append("();\n")
                .append("\t}");
    }

    private String instanceMethodName() {
        if (buildable.instanceMethodName().isEmpty()) {
            return "getInstance";
        }
        return firstLower(buildable.instanceMethodName());
    }

    private void generateBuilderBuildMethod(final StringBuilder sb, final String simpleClassName, final boolean noArgCtorIsNonPublic) {
        sb.append("\t@Override\n")
                .append("\tpublic ").append(simpleClassName).append(" ").append(buildMethodName()).append("(){\n");
        if (!noArgCtorIsNonPublic) {
            sb.append("\t\tfinal ").append(simpleClassName).append(" out = new ").append(simpleClassName).append("();\n");
        } else {
            sb.append("\t\t" + simpleClassName + " out = null;\n")
                    .append("\t\ttry{\n")
                    .append("\t\t\tjava.lang.reflect.Constructor ctor = " + simpleClassName + ".class.getDeclaredConstructor();\n")
                    .append("\t\t\tctor.setAccessible(true);\n")
                    .append("\t\t\tout  = (" + simpleClassName + ") ctor.newInstance();\n")
                    .append("\t\t} catch (InstantiationException  | java.lang.reflect.InvocationTargetException  | NoSuchMethodException  | IllegalAccessException ex) {\n")
                    .append("\t\t\tthrow new RuntimeException(ex);\n")
                    .append("\t\t}\n");
        }


        for (FluderCandidate cf : requiredCandidates) {
            candidateAssignInBuild(sb, simpleClassName, cf);
        }
        for (FluderCandidate cf : optionalCandidates) {
            candidateAssignInBuild(sb, simpleClassName, cf);
        }
        sb.append("\t\treturn out;\n")
                .append("\t}\n\n");
    }

    private String buildMethodName() {
        return buildable.buildMethodName().isEmpty() ? "build" : firstLower(buildable.buildMethodName());
    }

    private void generateBuilderOptionalSettersImplementations(final StringBuilder sb, final String simpleClassName) {
        for (FluderCandidate cOpt : optionalCandidates) {
            generateBuilderSetter(sb, creatorName(simpleClassName), cOpt);
        }
    }

    private void generateBuilderSetter(final StringBuilder sb, final String typeName, final FluderCandidate candidate) {
        sb.append("\t@Override\n")
                .append("\tpublic ").append(typeName).append(" set").append(candidate.setterName()).append("(").append(candidate.setterType()).append(" in){\n");
        if (candidate.isNonnull()) {
            sb.append("\t\tif(in==null){\n")
                    .append("\t\t\tthrow new IllegalArgumentException(\"set").append(candidate.setterName()).append(" can't be called with a null parameter. The target field is marked as @Nonnull\");\n")
                    .append("\t\t}\n");
        }
        sb.append("\t\tthis.").append(candidate.fieldName()).append(" = in;\n");
        if (candidate.isOptional()) {
            sb.append("\t\tthis.").append(candidate.fieldName()).append("Setted = true;\n");
        }
        sb.append("\t\treturn this;\n")
                .append("\t}\n\n");
    }

    private void generateBuilderSettersImplementations(final StringBuilder sb, final String simpleClassName) {
        final Iterator<FluderCandidate> itImplSet = requiredCandidates.iterator();
        FluderCandidate c = itImplSet.next();
        while (itImplSet.hasNext()) {
            final FluderCandidate next = itImplSet.next();
            final String typeName = next.intfName();
            generateBuilderSetter(sb, typeName, c);
            c = next;
        }
        generateBuilderSetter(sb, creatorName(simpleClassName), c);
    }

    private void generateBuilderCtor(final StringBuilder sb, final String simpleClassName) {
        sb.append("\n\tprivate ").append(builderName(simpleClassName)).append("(){\n")
                .append("\t\tsuper();\n")
                .append("\t}\n\n");
    }

    private void generateBuilderFields(final StringBuilder sb) {
        for (FluderCandidate c : requiredCandidates) {
            sb.append("\tprivate ").append(c.isNonnull() ? "@javax.annotation.Nonnull " : "").append(c.fieldSignature()).append(";\n");
        }
        for (FluderCandidate c : optionalCandidates) {
            sb.append("\tprivate ").append(c.fieldSignature()).append(";\n");
            sb.append("\tprivate boolean ").append(c.fieldName()).append("Setted;\n");
        }
    }

    private void generateBuilderImplements(final StringBuilder sb, final String simpleClassName) {
        for (FluderCandidate requiredCandidate : requiredCandidates) {
            sb.append(requiredCandidate.intfName()).append(", ");
        }
        sb.append(creatorName(simpleClassName));
    }

    private void generateCreatorInterface(final String packageName, final String simpleClassName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        final String creatorName = creatorName(simpleClassName);
        sb.append("public interface ").append(creatorName).append("{\n\n");
        generateCreatorSettersForOptionalCandidates(sb, creatorName);
        generateCreatorBuildMethod(sb, simpleClassName);
        sb.append("\n}");
        files.add(new FluderFile(creatorName, sb.toString()));
    }

    private void generateCreatorBuildMethod(final StringBuilder sb, final String simpleClassName) {
        sb.append("\t").append(simpleClassName).append(" ").append(buildMethodName()).append("();\n");
    }

    private void generateCreatorSettersForOptionalCandidates(final StringBuilder sb, final String creatorName) {
        for (FluderCandidate optionalCandidate : optionalCandidates) {
            sb.append("\t").append(creatorName).append(" set").append(optionalCandidate.setterName()).append("(").append(optionalCandidate.setterType()).append(" in);").append("\n\n");
        }
    }

    private void generateRequiredInterfaces(final String packageName, final String simpleClassName) {
        final Iterator<FluderCandidate> it = requiredCandidates.iterator();
        FluderCandidate candidate = it.next();
        while (it.hasNext()) {
            if (!candidate.isOptional()) {
                final FluderCandidate next = it.next();
                generateRequiredInterface(packageName, simpleClassName, next.intfName(), candidate);
                candidate = next;
            } else {
                candidate = it.next();
            }
        }
        generateRequiredInterface(packageName, simpleClassName, creatorName(simpleClassName), candidate);
    }


    private void generateRequiredInterface(final String packageName, final String simpleClassName, final String intfName, final FluderCandidate candidate) {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";\n\n");
        sb.append("public interface ").append(candidate.intfName()).append("{\n");
        sb.append("\t").append(intfName).append(" set").append(candidate.setterName()).append("(").append(candidate.setterType()).append(" in);").append("\n");
        sb.append("}");
        files.add(new FluderFile(candidate.intfName(), sb.toString()));
    }

    private void sortRequiredAndOptionalCandidates(final List<FluderCandidate> candidatesOrg) {
        for (FluderCandidate c : candidatesOrg) {
            if (!c.isOptional()) {
                requiredCandidates.add(c);
            } else {
                optionalCandidates.add(c);
            }
        }
    }

    private void orderCandidates(final List<FluderCandidate> candidatesOrg) {
        Collections.sort(candidatesOrg, new Comparator<FluderCandidate>() {
            @Override
            public int compare(FluderCandidate o1, FluderCandidate o2) {
                return Integer.compare(o1.order(), o2.order());
            }
        });
    }

    private void candidateAssignInBuild(final StringBuilder sb, final String simpleClassName, final FluderCandidate cf) {
        String value = cf.fieldName();
        if (cf.isOptional()) {
            if (cf.defaultValue() != null && !cf.defaultValue().equals("\0")) {
                value = "(" + cf.fieldName() + "Setted ? " + value + " : " + cf.defaultValue() + ")";
            } else {
                sb.append("\t\tif(").append(cf.fieldName()).append("Setted){\n");
            }
        }
        if (!cf.isPrivate()) {
            sb.append("\t\tout.").append(cf.fieldName()).append("=").append(value).append(";\n");
        } else {
            sb.append("\t\ttry {\n")
                    .append("\t\t\tfinal java.lang.reflect.Field f = ").append(simpleClassName).append(".class.getDeclaredField(\"").append(cf.fieldName()).append("\")").append(";\n")
                    .append("\t\t\tf.setAccessible(true);\n")
                    .append("\t\t\tf.set(out,").append(value).append(");\n")
                    .append("\t\t} catch (NoSuchFieldException | IllegalAccessException ex) {\n")
                    .append("\t\t\tthrow new RuntimeException(ex);\n")
                    .append("\t\t}\n");
        }
        if (cf.defaultValue() == null || cf.defaultValue().equals("\0")) {
            sb.append("\t\t}\n");
        }
    }
}
