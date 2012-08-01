import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public abstract class aqg extends apm {
    protected static awg a = new awg();
    protected int b = 176;
    protected int c = 166;
    public ou d;
    protected int m;
    protected int n;
    private Class<?> tmiControllerClass = null;
    private Class<?> tmiUtilsClass = null;
    private Object tmiController = null;

    public aqg(ou paramou) {
        this.d = paramou;

        // InvTweaks + TMI compatibility
        if (tmi()) {
            try {
                this.getClass();
                tmiControllerClass = Class.forName("TMIController");
                tmiController = tmiControllerClass.getConstructor(aqg.class, awg.class)
                        .newInstance(this, aqg.a);
                tmiUtilsClass = Class.forName("TMIUtils");
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("InvTweaks failed to set up TMI compatibility", e);
            }
        }
    }

    private boolean tmi() {
        return tmiControllerClass != null || InvTweaks.classExists("TMIController");
    }

    public void w_() {
        super.w_();
        this.e.g.bA = this.d;

        this.m = ((this.f - this.b) / 2);
        this.n = ((this.g - this.c) / 2);
    }

    public void a(int paramInt1, int paramInt2, float paramFloat) {
        v_();
        int i = this.m;
        int j = this.n;

        a(paramFloat, paramInt1, paramInt2);

        GL11.glDisable(32826);
        anf.a();
        GL11.glDisable(2896);
        GL11.glDisable(2929);

        super.a(paramInt1, paramInt2, paramFloat);

        anf.c();

        GL11.glPushMatrix();
        GL11.glTranslatef(i, j, 0.0F);

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(32826);

        pq localObject1 = null;

        int k = 240;
        int i1 = 240;
        ayx.a(ayx.b, k / 1.0F, i1 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Object localObject2;
        for (k = 0; k < this.d.b.size(); k++) {
            localObject2 = (pq) this.d.b.get(k);

            a((pq) localObject2);

            if (a((pq) localObject2, paramInt1, paramInt2)) {
                localObject1 = (pq) localObject2;

                GL11.glDisable(2896);
                GL11.glDisable(2929);

                int i2 = ((pq) localObject2).e;
                int i3 = ((pq) localObject2).f;
                a(i2, i3, i2 + 16, i3 + 16, -2130706433, -2130706433);
                GL11.glEnable(2896);
                GL11.glEnable(2929);
            }
        }

        // InvTweaks + TMI compatibility
        if (tmi()) {
            try {
                int i2 = Mouse.getEventX() * this.f / Minecraft.x().c;
                int i3 = this.g - Mouse.getEventY() * this.g / Minecraft.x().d - 1;
                tmiControllerClass.getMethod("handleScrollWheel", Integer.TYPE, Integer.TYPE)
                        .invoke(this.tmiController, i2, i3);

                GL11.glTranslatef(-i, -j, 0.0F);
                tmiControllerClass.getMethod("onEnterFrame", Integer.TYPE, Integer.TYPE,
                        Integer.TYPE, Integer.TYPE).invoke(this.tmiController, paramInt1,
                        paramInt2, this.b, this.c);
                tmiControllerClass.getMethod("showToolTip", Integer.TYPE, Integer.TYPE).invoke(
                        this.tmiController, paramInt1, paramInt2);
                GL11.glTranslatef(i, j, 0.0F);
                GL11.glEnable(2896);
                GL11.glEnable(2929);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        g();

        oe localoe = this.e.g.by;
        ri localri = localoe.o();
        if (localoe.o() != null) {
            // InvTweaks + TMI compatibility
            if (tmi()) {
                try {
                    tmiUtilsClass.getMethod("getValidItem", ri.class).invoke(null, localoe.o());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            GL11.glTranslatef(0.0F, 0.0F, 32.0F);
            this.i = 200.0F;
            a.b = 200.0F;

            a.a(this.k, this.e.o, localri, paramInt1 - i - 8, paramInt2 - j - 8);
            a.b(this.k, this.e.o, localri, paramInt1 - i - 8, paramInt2 - j - 8);
            this.i = 0.0F;
            a.b = 0.0F;
        }

        if ((localoe.o() == null) && (localObject1 != null) && (localObject1.d())) {
            localObject2 = localObject1.c();
            a((ri) localObject2, paramInt1 - i, paramInt2 - j);
        }

        GL11.glPopMatrix();

        GL11.glEnable(2896);
        GL11.glEnable(2929);
        anf.b();
    }

    @SuppressWarnings("unchecked")
    protected void a(ri paramri, int paramInt1, int paramInt2) {
        GL11.glDisable(32826);
        anf.a();
        GL11.glDisable(2896);
        GL11.glDisable(2929);

        List<String> localList = paramri.r();

        // InvTweaks + TMI compatibility
        if (tmi()) {
            try {
                localList = (List<String>) tmiUtilsClass.getMethod("itemDisplayNameMultiline",
                        ri.class, Boolean.TYPE).invoke(null, paramri, false);// TMIConfig.getInstance().isEnabled()
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!localList.isEmpty()) {
            int i = 0;
            for (String str1 : localList) {
                int i1 = this.k.a(str1);
                if (i1 > i)
                    i = i1;
            }

            int j = paramInt1 + 12;
            int k = paramInt2 - 12;

            int i1 = i;
            int i2 = 8;
            if (localList.size() > 1) {
                i2 += 2 + (localList.size() - 1) * 10;
            }

            this.i = 300.0F;
            a.b = 300.0F;

            int i3 = -267386864;
            a(j - 3, k - 4, j + i1 + 3, k - 3, i3, i3);
            a(j - 3, k + i2 + 3, j + i1 + 3, k + i2 + 4, i3, i3);

            a(j - 3, k - 3, j + i1 + 3, k + i2 + 3, i3, i3);
            a(j - 4, k - 3, j - 3, k + i2 + 3, i3, i3);
            a(j + i1 + 3, k - 3, j + i1 + 4, k + i2 + 3, i3, i3);

            int i4 = 1347420415;
            int i5 = (i4 & 0xFEFEFE) >> 1 | i4 & 0xFF000000;

            a(j - 3, k - 3 + 1, j - 3 + 1, k + i2 + 3 - 1, i4, i5);
            a(j + i1 + 2, k - 3 + 1, j + i1 + 3, k + i2 + 3 - 1, i4, i5);

            a(j - 3, k - 3, j + i1 + 3, k - 3 + 1, i4, i4);
            a(j - 3, k + i2 + 2, j + i1 + 3, k + i2 + 3, i5, i5);

            for (int i6 = 0; i6 < localList.size(); i6++) {
                String str2 = (String) localList.get(i6);
                if (i6 == 0)
                    str2 = "§" + Integer.toHexString(paramri.t().e) + str2;
                else
                    str2 = "§7" + str2;
                this.k.a(str2, j, k, -1);
                if (i6 == 0)
                    k += 2;
                k += 10;
            }

            this.i = 0.0F;
            a.b = 0.0F;
        }
    }

    protected void a(String paramString, int paramInt1, int paramInt2) {
        GL11.glDisable(32826);
        anf.a();
        GL11.glDisable(2896);
        GL11.glDisable(2929);

        int i = this.k.a(paramString);

        int j = paramInt1 + 12;
        int k = paramInt2 - 12;

        int i1 = i;
        int i2 = 8;

        this.i = 300.0F;
        a.b = 300.0F;

        int i3 = -267386864;
        a(j - 3, k - 4, j + i1 + 3, k - 3, i3, i3);
        a(j - 3, k + i2 + 3, j + i1 + 3, k + i2 + 4, i3, i3);

        a(j - 3, k - 3, j + i1 + 3, k + i2 + 3, i3, i3);
        a(j - 4, k - 3, j - 3, k + i2 + 3, i3, i3);
        a(j + i1 + 3, k - 3, j + i1 + 4, k + i2 + 3, i3, i3);

        int i4 = 1347420415;
        int i5 = (i4 & 0xFEFEFE) >> 1 | i4 & 0xFF000000;

        a(j - 3, k - 3 + 1, j - 3 + 1, k + i2 + 3 - 1, i4, i5);
        a(j + i1 + 2, k - 3 + 1, j + i1 + 3, k + i2 + 3 - 1, i4, i5);

        a(j - 3, k - 3, j + i1 + 3, k - 3 + 1, i4, i4);
        a(j - 3, k + i2 + 2, j + i1 + 3, k + i2 + 3, i5, i5);

        this.k.a(paramString, j, k, -1);

        this.i = 0.0F;
        a.b = 0.0F;
    }

    protected void g() {
    }

    protected abstract void a(float paramFloat, int paramInt1, int paramInt2);

    private void a(pq parampq) {
        int i = parampq.e;
        int j = parampq.f;
        ri localri = parampq.c();

        int k = 0;

        this.i = 100.0F;
        a.b = 100.0F;
        if (localri == null) {
            int i1 = parampq.b();
            if (i1 >= 0) {
                GL11.glDisable(2896);
                this.e.o.b(this.e.o.b("/gui/items.png"));
                b(i, j, i1 % 16 * 16, i1 / 16 * 16, 16, 16);
                GL11.glEnable(2896);
                k = 1;
            }
        }

        if (k == 0) {
            GL11.glEnable(2929);
            a.a(this.k, this.e.o, localri, i, j);
            a.b(this.k, this.e.o, localri, i, j);
        }
        a.b = 0.0F;
        this.i = 0.0F;
    }

    private pq b(int paramInt1, int paramInt2) {
        for (int i = 0; i < this.d.b.size(); i++) {
            pq localpq = (pq) this.d.b.get(i);
            if (a(localpq, paramInt1, paramInt2))
                return localpq;
        }
        return null;
    }

    protected void a(int paramInt1, int paramInt2, int paramInt3) {
        super.a(paramInt1, paramInt2, paramInt3);

        // InvTweaks + TMI compatibility
        if (tmi()) {
            int i = this.b;
            int j = this.c;
            int k = this.m;
            int i1 = this.n;
            pq localpq = b(paramInt1, paramInt2);
            Minecraft localMinecraft = Minecraft.x();
            ou localou = this.d;
            boolean bool = (paramInt1 >= k) && (paramInt2 >= i1) && (paramInt1 <= k + i)
                    && (paramInt2 <= i1 + j);
            if ((paramInt3 >= 0) && (paramInt3 <= 2)) {
                try {
                    this.tmiControllerClass.getMethod("replacementClickHandler", Integer.TYPE,
                            Integer.TYPE, Integer.TYPE, Boolean.TYPE, pq.class, Minecraft.class,
                            ou.class).invoke(this.tmiController, paramInt1, paramInt2, paramInt3,
                            bool, localpq, localMinecraft, localou);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if ((paramInt3 == 0) || (paramInt3 == 1)) {
                pq localpq = b(paramInt1, paramInt2);

                int i = this.m;
                int j = this.n;
                int k = (paramInt1 < i) || (paramInt2 < j) || (paramInt1 >= i + this.b)
                        || (paramInt2 >= j + this.c) ? 1 : 0;

                int i1 = -1;
                if (localpq != null)
                    i1 = localpq.d;

                if (k != 0) {
                    i1 = -999;
                }

                if (i1 != -1) {
                    boolean bool = (i1 != -999)
                            && ((Keyboard.isKeyDown(42)) || (Keyboard.isKeyDown(54)));
                    a(localpq, i1, paramInt3, bool);
                }
            }
        }
    }

    private boolean a(pq parampq, int paramInt1, int paramInt2) {
        return c(parampq.e, parampq.f, 16, 16, paramInt1, paramInt2);
    }

    protected boolean c(int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5,
            int paramInt6) {
        int i = this.m;
        int j = this.n;
        paramInt5 -= i;
        paramInt6 -= j;

        return (paramInt5 >= paramInt1 - 1) && (paramInt5 < paramInt1 + paramInt3 + 1)
                && (paramInt6 >= paramInt2 - 1) && (paramInt6 < paramInt2 + paramInt4 + 1);
    }

    protected void a(pq parampq, int paramInt1, int paramInt2, boolean paramBoolean) {
        if (parampq != null)
            paramInt1 = parampq.d;

        // Inventory Tweaks: filter default action if there's an InvTweaks
        // shortcut to trigger
        if (InvTweaks.getConfigManager().getShortcutsHandler().computeShortcutToTrigger() == null) {
            this.e.b.a(this.d.c, paramInt1, paramInt2, paramBoolean, this.e.g);
        }

    }

    protected void a(char paramChar, int paramInt) {
        // InvTweaks + TMI compatibility
        if (tmi()) {
            try {
                boolean result = (Boolean) this.tmiControllerClass.getMethod("handleKeypress", Character.TYPE, Integer.TYPE).invoke(this.tmiController, paramChar, paramInt);
                if ((!result) && ((paramInt == 1) || (paramInt == this.e.y.B.d)))
                    this.e.g.j();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if ((paramInt == 1) || (paramInt == this.e.y.B.d))
                this.e.g.j();
        }
    }

    public void b() {
        if (this.e.g == null)
            return;
        this.d.a(this.e.g);
    }

    public boolean f() {
        return false;
    }

    public void c() {
        super.c();
        if ((!this.e.g.S()) || (this.e.g.L))
            this.e.g.j();
    }
}
