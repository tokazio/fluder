package fr.tokazio.fluder.core;

import fr.tokazio.fluder.annotations.Buildable;

import java.util.*;


/**
 * https://dzone.com/articles/fluent-builder-pattern
 */
public class FluderJavaGenerator implements FluderCodeGenerator {

    private Buildable buildable;
    private final List<FluderCandidate> requiredCandidates = new LinkedList<>();
    private final List<FluderCandidate> optionalCandidates = new LinkedList<>();
    private final List<FluderFile> files = new LinkedList<>();

    @Override
    public List<FluderFile> generate(final FluderClass fluderClass) {
        this.buildable = fluderClass.buildable();
        final List<FluderCandidate> orderedCandidates = orderCandidates(fluderClass.getCandidates());
        sortRequiredAndOptionalCandidates(orderedCandidates);
        generateRequiredInterfaces(fluderClass);
        generateCreatorInterface(fluderClass);
        generateBuilderClass(fluderClass);
        return files;
    }

    private void generateBuilderClass(final FluderClass fluderClass) {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(fluderClass.getPackageName()).append(";\n\n");
        final String builderName = builderName(buildable.builderName(), fluderClass.getSimpleName());
        sb.append("public class ").append(builderName).append(" implements ");
        generateBuilderImplements(sb, fluderClass.getSimpleName());
        sb.append("{\n\n");
        generateBuilderFields(sb);
        generateBuilderCtor(sb, fluderClass.getSimpleName());
        generateBuilderSettersImplementations(sb, fluderClass.getSimpleName());
        generateBuilderOptionalSettersImplementations(sb, fluderClass.getSimpleName());
        generateBuilderBuildMethod(sb, fluderClass.getSimpleName(), fluderClass.noArgCtorIsNotPublic());
        generateBuilderSingleton(sb, builderName);
        sb.append("\n}");
        files.add(new FluderFile(builderName, sb.toString()));
    }

    String builderName(final String builderName, final String simpleClassName) {
        if (builderName.isEmpty()) {
            return FluderUtils.firstUpper(simpleClassName) + "Builder";
        }
        if (builderName.contains("$")) {
            return FluderUtils.firstUpper(builderName.replace("$", simpleClassName));
        }
        return FluderUtils.firstUpper(builderName);
    }

    String creatorName(final String creatorName, final String simpleClassName) {
        if (creatorName.isEmpty()) {
            return FluderUtils.firstUpper(simpleClassName) + "Creator";
        }
        if (creatorName.contains("$")) {
            return FluderUtils.firstUpper(creatorName.replace("$", simpleClassName));
        }
        return FluderUtils.firstUpper(creatorName);
    }

    private void generateBuilderSingleton(final StringBuilder sb, final String builderName) {
        sb.append("\tpublic static ").append(requiredCandidates.get(0).intfName()).append(" ").append(instanceMethodName(buildable.instanceMethodName())).append("(){\n")
                .append("\t\treturn new ").append(builderName).append("();\n")
                .append("\t}");
    }

    String instanceMethodName(final String instanceMethodName) {
        if (instanceMethodName.isEmpty()) {
            return "getInstance";
        }
        return FluderUtils.firstLower(instanceMethodName);
    }

    private void generateBuilderBuildMethod(final StringBuilder sb, final String simpleClassName, final boolean noArgCtorIsNonPublic) {
        sb.append("\t@Override\n")
                .append("\tpublic ").append(simpleClassName).append(" ").append(buildMethodName(buildable.buildMethodName())).append("(){\n");
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

    String buildMethodName(final String methodName) {
        return methodName.isEmpty() ? "build" : FluderUtils.firstLower(methodName);
    }

    private void generateBuilderOptionalSettersImplementations(final StringBuilder sb, final String simpleClassName) {
        for (FluderCandidate cOpt : optionalCandidates) {
            generateBuilderSetter(sb, creatorName(buildable.creatorName(), simpleClassName), cOpt);
        }
    }

    private void generateBuilderSetter(final StringBuilder sb, final String typeName, final FluderCandidate candidate) {
        sb.append("\t@Override\n")
                .append("\tpublic ").append(typeName).append(" set").append(candidate.setterName()).append("(").append(candidate.setterType()).append(" in){\n");
        for (Validation va : candidate.validations()) {
            va.validate(sb, candidate);
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
        generateBuilderSetter(sb, creatorName(buildable.creatorName(), simpleClassName), c);
    }

    private void generateBuilderCtor(final StringBuilder sb, final String simpleClassName) {
        sb.append("\n\tprivate ").append(builderName(buildable.builderName(), simpleClassName)).append("(){\n")
                .append("\t\tsuper();\n")
                .append("\t}\n\n");
    }

    private void generateBuilderFields(final StringBuilder sb) {
        for (FluderCandidate c : requiredCandidates) {
            //TODO replicate annotations from target field ? the validations ones ? .append(c.isNonnull() ? "@javax.annotation.Nonnull " : "")
            sb.append("\tprivate ").append(c.fieldSignature()).append(";\n");
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
        sb.append(creatorName(buildable.creatorName(), simpleClassName));
    }

    private void generateCreatorInterface(final FluderClass fluderClass) {
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(fluderClass.getPackageName()).append(";\n\n");
        final String creatorName = creatorName(buildable.creatorName(), fluderClass.getSimpleName());
        sb.append("public interface ").append(creatorName).append("{\n\n");
        generateCreatorSettersForOptionalCandidates(sb, creatorName);
        generateCreatorBuildMethod(sb, fluderClass.getSimpleName());
        sb.append("\n}");
        files.add(new FluderFile(creatorName, sb.toString()));
    }

    private void generateCreatorBuildMethod(final StringBuilder sb, final String simpleClassName) {
        sb.append("\t").append(simpleClassName).append(" ").append(buildMethodName(buildable.buildMethodName())).append("();\n");
    }

    private void generateCreatorSettersForOptionalCandidates(final StringBuilder sb, final String creatorName) {
        for (FluderCandidate optionalCandidate : optionalCandidates) {
            sb.append("\t").append(creatorName).append(" set").append(optionalCandidate.setterName()).append("(").append(optionalCandidate.setterType()).append(" in);").append("\n\n");
        }
    }

    private void generateRequiredInterfaces(final FluderClass fluderClass) {
        final Iterator<FluderCandidate> it = requiredCandidates.iterator();
        FluderCandidate candidate = it.next();
        while (it.hasNext()) {
            if (!candidate.isOptional()) {
                final FluderCandidate next = it.next();
                generateRequiredInterface(fluderClass.getPackageName(), next.intfName(), candidate);
                candidate = next;
            } else {
                candidate = it.next();
            }
        }
        generateRequiredInterface(fluderClass.getPackageName(), creatorName(buildable.creatorName(), fluderClass.getSimpleName()), candidate);
    }


    private void generateRequiredInterface(final String packageName, final String intfName, final FluderCandidate candidate) {
        String sb = "package " + packageName + ";\n\n" +
                "public interface " + candidate.intfName() + "{\n" +
                "\t" + intfName + " set" + candidate.setterName() + "(" + candidate.setterType() + " in);" + "\n" +
                "}";
        files.add(new FluderFile(candidate.intfName(), sb));
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

    private List<FluderCandidate> orderCandidates(final List<FluderCandidate> candidatesOrg) {
        final List<FluderCandidate> ordered = new ArrayList<>(candidatesOrg);
        ordered.sort(Comparator.comparingInt(FluderCandidate::order));
        return ordered;
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
