var Opcodes = Java.type("org.objectweb.asm.Opcodes");
var FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");

// var AbstractListScroller = Java.type("me.shedaniel.smoothscrollingeverywhere.AbstractListScroller");

function initializeCoreMod() {
    return {
        "smooth-scrolling-everywhere": {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.gui.widget.list.AbstractList'
            },
            'transformer': function (classNode) {
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "smoothscrollingeverywhere_scrollVelocity", "D", null, 0.0));
                classNode.fields.add(new FieldNode(Opcodes.ACC_PUBLIC, "smoothscrollingeverywhere_scroller", "Lme/shedaniel/smoothscrollingeverywhere/api/RunSixtyTimesEverySec;", null, null));
                var methods = classNode.methods;
                for (m in methods) {
                    var method = methods[m];
                    var name = method.name;
                    if (name == "setScrollAmount") {
                        var instructions = method.instructions;
                        var first = instructions.get(0);
                        instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/shedaniel/smoothscrollingeverywhere/CustomAbstractList", "setScrollAmount", "(Lnet/minecraft/client/gui/widget/list/AbstractList;)V", false));
                    } else if (name == "mouseScrolled") {
                        var instructions = method.instructions;
                        var first = instructions.get(0);
                        instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        instructions.insertBefore(first, new VarInsnNode(Opcodes.DLOAD, 5));
                        instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/shedaniel/smoothscrollingeverywhere/CustomAbstractList", "mouseScrolled", "(Lnet/minecraft/client/gui/widget/list/AbstractList;D)V", false));
                        instructions.insertBefore(first, new InsnNode(Opcodes.ICONST_1));
                        instructions.insertBefore(first, new InsnNode(Opcodes.IRETURN));
                    } else if (name == "render") {
                        // TODO: Find a better method to edit the scroll bar
                        var instructions = method.instructions;
                        var insnArray = instructions.toArray();
                        for (i in insnArray) {
                            var insn = insnArray[i];
                            if (insn instanceof MethodInsnNode) {
                                if (insn.owner == "net/minecraft/client/gui/widget/list/AbstractList" &&
                                insn.name == "getMaxScroll" && insn.desc == "()I") {
                                    instructions.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                                    instructions.insertBefore(insn, new VarInsnNode(Opcodes.ILOAD, 1));
                                    instructions.insertBefore(insn, new VarInsnNode(Opcodes.ILOAD, 2));
                                    instructions.insertBefore(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, "me/shedaniel/smoothscrollingeverywhere/CustomAbstractList", "renderScrollbar", "(Lnet/minecraft/client/gui/widget/list/AbstractList;II)V", false));
                                    instructions.insertBefore(insn, new InsnNode(Opcodes.RETURN));
                                    break;
                                }
                            }
                        }
                    }
                }
                return classNode;
            }
        }
    }
}