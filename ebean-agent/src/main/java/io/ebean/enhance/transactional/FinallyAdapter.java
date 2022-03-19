package io.ebean.enhance.transactional;

import io.ebean.enhance.Transformer;
import io.ebean.enhance.asm.Label;
import io.ebean.enhance.asm.MethodVisitor;
import io.ebean.enhance.asm.commons.AdviceAdapter;

/**
 * FinallyAdapter for MethodAdapter that also enhances transactional methods)
 */
abstract class FinallyAdapter extends AdviceAdapter {

  private final Label startFinally = new Label();

  FinallyAdapter(MethodVisitor mv, int acc, String name, String desc) {
    super(Transformer.EBEAN_ASM_VERSION, mv, acc, name, desc);
  }

  void finallyVisitStart() {
    mv.visitLabel(startFinally);
  }

  protected void finallyVisitMaxs(int maxStack, int maxLocals) {
    Label endFinally = new Label();
    mv.visitTryCatchBlock(startFinally, endFinally, endFinally, null);
    mv.visitLabel(endFinally);
    onFinally(ATHROW);
    mv.visitInsn(ATHROW);
    mv.visitMaxs(maxStack, maxLocals);
  }

  @Override
  protected final void onMethodExit(int opcode) {
    if (opcode != ATHROW) {
      onFinally(opcode);
    }
  }

  abstract void onFinally(int opcode);

}
