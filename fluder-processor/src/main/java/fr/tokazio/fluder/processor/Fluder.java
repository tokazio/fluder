package fr.tokazio.fluder.processor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * https://dzone.com/articles/fluent-builder-pattern
 */
public class Fluder {

    public List<FluderFile> generate(String simpleClassName, List<FluderCandidate> candidates) {

        final List<FluderFile> files = new LinkedList<>();
        //generate intermediate intf
        Iterator<FluderCandidate> it = candidates.iterator();
        FluderCandidate candidate = it.next();
        while (it.hasNext()) {
            if (!candidate.isOptional()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("package ").append(candidate.pckName()).append(";\n\n");
                sb.append("public interface ").append(candidate.intfName()).append("{\n");
                final FluderCandidate next = it.next();
                String nextIntfName = next.intfName();
                final String javaCode = nextIntfName + " set" + candidate.setterName() + "(" + candidate.setterType() + " in);";
                sb.append("\t").append(javaCode).append("\n");
                sb.append("}");
                System.out.println(sb.toString());
                files.add(new FluderFile(candidate.intfName(), sb.toString()));
                candidate = next;
            } else {
                candidate = it.next();
            }
        }
        //generate last intf
        final StringBuilder sb = new StringBuilder();
        sb.append("package ").append(candidate.pckName()).append(";\n\n");
        sb.append("public interface ").append(candidate.intfName()).append("{\n");
        final String javaCode = simpleClassName + "Creator set" + candidate.setterName() + "(" + candidate.setterType() + " in);";
        sb.append("\t").append(javaCode).append("\n");
        sb.append("}");
        System.out.println(sb.toString());
        files.add(new FluderFile(candidate.intfName(), sb.toString()));
        //generate Creator intf
        final StringBuilder sbCreator = new StringBuilder();
        sbCreator.append("package ").append(candidate.pckName()).append(";\n\n");
        final String intfName = simpleClassName + "Creator";
        sbCreator.append("public interface ").append(intfName).append("{\n");
        for (FluderCandidate c : candidates) {
            if (c.isOptional()) {
                sbCreator.append("\t").append(intfName).append(" set").append(candidate.setterName()).append("(").append(candidate.setterType()).append(" in);").append("\n\n");
            }
        }
        final String build = simpleClassName + " build();";
        sbCreator.append("\t").append(build).append("\n");
        sbCreator.append("}");
        System.out.println(sb.toString());
        files.add(new FluderFile(intfName, sbCreator.toString()));
        //generate builder
        final StringBuilder builderStr = new StringBuilder();
        builderStr.append("package ").append(candidate.pckName()).append(";\n\n");
        builderStr.append("public class ").append(simpleClassName).append("Builder implements ");
        final Iterator<FluderCandidate> itImpl = candidates.iterator();
        while (itImpl.hasNext()) {
            final FluderCandidate c = itImpl.next();
            builderStr.append(c.intfName()).append(", ");
        }
        builderStr.append(simpleClassName).append("Creator");
        builderStr.append("{\n\n");
        for (FluderCandidate c : candidates) {
            builderStr.append("\tprivate ").append(c.fieldSignature()).append(";\n");
        }
        builderStr.append("\n\tprivate ").append(simpleClassName).append("Builder(){\n")
                .append("\t\tsuper();\n")
                .append("\t}\n\n");
        //implements setters
        final Iterator<FluderCandidate> itImplSet = candidates.iterator();
        FluderCandidate c = itImplSet.next();
        while (itImplSet.hasNext()) {
            FluderCandidate next = itImplSet.next();
            String typeName = next.intfName();
            builderStr.append("\t@Override\n")
                    .append("\tpublic ").append(typeName).append(" set").append(c.setterName()).append("(").append(c.setterType()).append(" in){\n");
            builderStr.append("\t\tthis.").append(c.fieldName()).append(" = in;\n");
            builderStr.append("\t\treturn this;\n")
                    .append("\t}\n\n");
            c = next;
        }
        //implements last setter
        builderStr.append("\t@Override\n")
                .append("\tpublic ").append(simpleClassName).append("Creator").append(" set").append(c.setterName()).append("(").append(c.setterType()).append(" in){\n");
        builderStr.append("\t\tthis.").append(c.fieldName()).append(" = in;\n");
        builderStr.append("\t\treturn this;\n")
                .append("\t}\n\n");
        //implements build
        builderStr.append("\t@Override\n")
                .append("\tpublic ").append(simpleClassName).append(" build(){\n")
                .append("\t\tfinal ").append(simpleClassName).append(" out = new ").append(simpleClassName).append("();\n");
        for (FluderCandidate cf : candidates) {
            if (!cf.isPrivate()) {
                builderStr.append("\t\tout.").append(cf.fieldName()).append("=").append(cf.fieldName()).append(";\n");
            } else {
                builderStr.append("\t\ttry {\n")
                        .append("\t\t\tfinal java.lang.reflect.Field f = " + simpleClassName + ".class.getDeclaredField(\"" + cf.fieldName() + "\")").append(";\n")
                        .append("\t\t\tf.setAccessible(true);\n")
                        .append("\t\t\tf.set(out," + cf.fieldName() + ");\n")
                        .append("\t\t} catch (NoSuchFieldException | IllegalAccessException e) {\n")
                        .append("\t\t\te.printStackTrace();\n")
                        .append("\t\t}\n");
            }
        }
        builderStr.append("\t\treturn out;\n")
                .append("\t}\n\n");

        //
        builderStr.append("\tpublic static ").append(candidates.get(0).intfName()).append(" getInstance(){\n").append("\t\treturn new ").append(simpleClassName).append("Builder();\n")
                .append("\t}\n");
        builderStr.append("}");
        System.out.println(builderStr.toString());
        files.add(new FluderFile(simpleClassName + "Builder", builderStr.toString()));
        return files;
    }


}
