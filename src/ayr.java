import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class ayr extends apm {
    private static final Random a = new Random();

    private float b = 0.0F;
    private String c;
    private aog d;
    private int m = 0;
    private int n;
    private static final String[] o = { "/title/bg/panorama0.png", "/title/bg/panorama1.png",
            "/title/bg/panorama2.png", "/title/bg/panorama3.png", "/title/bg/panorama4.png",
            "/title/bg/panorama5.png" };

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ayr() {
        this.c = "missingno";

        // InvTweaks init hook
        try {
            ModLoader_InvTweaks.init();
        } catch (Exception e) {
            System.out.println("Failed to initialize fake ModLoader by InvTweaks" + e.getMessage());
        }
        BufferedReader localBufferedReader = null;
        try {
            ArrayList localArrayList = new ArrayList();
            localBufferedReader = new BufferedReader(new InputStreamReader(
                    ayr.class.getResourceAsStream("/title/splashes.txt"), Charset.forName("UTF-8")));
            String str;
            while ((str = localBufferedReader.readLine()) != null) {
                str = str.trim();
                if (str.length() > 0) {
                    localArrayList.add(str);
                }

            }

            do {
                this.c = ((String) localArrayList.get(a.nextInt(localArrayList.size())));
            } while (this.c.hashCode() == 125780783);
        } catch (IOException localIOException3) {
        } finally {
            if (localBufferedReader != null)
                try {
                    localBufferedReader.close();
                } catch (IOException localIOException4) {
                }
        }
        this.b = a.nextFloat();
    }

    public void c() {
        this.m += 1;
    }

    public boolean f() {
        return false;
    }

    protected void a(char paramChar, int paramInt) {
    }

    @SuppressWarnings("unchecked")
    public void w_() {
        this.n = this.e.o.a(new BufferedImage(256, 256, 2));

        Calendar localCalendar = Calendar.getInstance();
        localCalendar.setTime(new Date());

        if ((localCalendar.get(2) + 1 == 11) && (localCalendar.get(5) == 9))
            this.c = "Happy birthday, ez!";
        else if ((localCalendar.get(2) + 1 == 6) && (localCalendar.get(5) == 1))
            this.c = "Happy birthday, Notch!";
        else if ((localCalendar.get(2) + 1 == 12) && (localCalendar.get(5) == 24))
            this.c = "Merry X-mas!";
        else if ((localCalendar.get(2) + 1 == 1) && (localCalendar.get(5) == 1)) {
            this.c = "Happy new year!";
        }

        ak localak = ak.a();

        int i = this.g / 4 + 48;

        if (this.e.q())
            b(i, 24, localak);
        else {
            a(i, 24, localak);
        }
        this.h.add(new aog(3, this.f / 2 - 100, i + 48, localak.b("menu.mods")));

        if (this.e.m) {
            this.h.add(new aog(0, this.f / 2 - 100, i + 72, localak.b("menu.options")));
        } else {
            this.h.add(new aog(0, this.f / 2 - 100, i + 72 + 12, 98, 20, localak.b("menu.options")));
            this.h.add(new aog(4, this.f / 2 + 2, i + 72 + 12, 98, 20, localak.b("menu.quit")));
        }
        this.h.add(new apc(5, this.f / 2 - 124, i + 72 + 12));
    }

    @SuppressWarnings("unchecked")
    private void a(int paramInt1, int paramInt2, ak paramak) {
        this.h.add(new aog(1, this.f / 2 - 100, paramInt1, paramak.b("menu.singleplayer")));
        this.h.add(new aog(2, this.f / 2 - 100, paramInt1 + paramInt2 * 1, paramak
                .b("menu.multiplayer")));
    }

    @SuppressWarnings("unchecked")
    private void b(int paramInt1, int paramInt2, ak paramak) {
        this.h.add(new aog(11, this.f / 2 - 100, paramInt1, paramak.b("menu.playdemo")));
        this.h.add(this.d = new aog(12, this.f / 2 - 100, paramInt1 + paramInt2 * 1, paramak
                .b("menu.resetdemo")));

        aef localaef = this.e.d();
        aec localaec = localaef.c("Demo_World");
        if (localaec == null)
            this.d.g = false;
    }

    protected void a(aog paramaog) {
        if (paramaog.f == 0) {
            this.e.a(new aph(this, this.e.y));
        }
        if (paramaog.f == 5) {
            this.e.a(new apd(this, this.e.y));
        }
        if (paramaog.f == 1) {
            this.e.a(new app(this));
        }
        if (paramaog.f == 2) {
            this.e.a(new aoz(this));
        }
        if (paramaog.f == 3) {
            this.e.a(new ayk(this));
        }
        if (paramaog.f == 4) {
            this.e.g();
        }
        if (paramaog.f == 11) {
            this.e.a("Demo_World", "Demo_World", gi.a);
        }
        if (paramaog.f == 12) {
            aef localaef = this.e.d();
            aec localaec = localaef.c("Demo_World");
            if (localaec != null) {
                aol localaol = app.a(this, localaec.j(), 12);
                this.e.a(localaol);
            }
        }
    }

    public void a(boolean paramBoolean, int paramInt) {
        if ((paramBoolean) && (paramInt == 12)) {
            aef localaef = this.e.d();
            localaef.d();
            localaef.e("Demo_World");

            this.e.a(this);
        }
    }

    private void b(int paramInt1, int paramInt2, float paramFloat) {
        avd localavd = avd.a;

        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GLU.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);

        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glDisable(2884);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(770, 771);
        int i = 8;

        for (int j = 0; j < i * i; j++) {
            GL11.glPushMatrix();
            float f1 = (j % i / i - 0.5F) / 64.0F;
            float f2 = (j / i / i - 0.5F) / 64.0F;
            float f3 = 0.0F;
            GL11.glTranslatef(f1, f2, f3);

            GL11.glRotatef(ig.a((this.m + paramFloat) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-(this.m + paramFloat) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int k = 0; k < 6; k++) {
                GL11.glPushMatrix();
                if (k == 1)
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 2)
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                if (k == 3)
                    GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                if (k == 4)
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                if (k == 5)
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                GL11.glBindTexture(3553, this.e.o.b(o[k]));
                localavd.b();
                localavd.a(16777215, 255 / (j + 1));
                float f4 = 0.0F;
                localavd.a(-1.0D, -1.0D, 1.0D, 0.0F + f4, 0.0F + f4);
                localavd.a(1.0D, -1.0D, 1.0D, 1.0F - f4, 0.0F + f4);
                localavd.a(1.0D, 1.0D, 1.0D, 1.0F - f4, 1.0F - f4);
                localavd.a(-1.0D, 1.0D, 1.0D, 0.0F + f4, 1.0F - f4);
                localavd.a();
                GL11.glPopMatrix();
            }
            GL11.glPopMatrix();
            GL11.glColorMask(true, true, true, false);
        }
        localavd.b(0.0D, 0.0D, 0.0D);
        GL11.glColorMask(true, true, true, true);

        GL11.glMatrixMode(5889);
        GL11.glPopMatrix();
        GL11.glMatrixMode(5888);
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glEnable(2884);

        GL11.glEnable(3008);
        GL11.glEnable(2929);
    }

    private void a(float paramFloat) {
        GL11.glBindTexture(3553, this.n);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);

        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColorMask(true, true, true, false);
        avd localavd = avd.a;
        localavd.b();

        int i = 3;
        for (int j = 0; j < i; j++) {
            localavd.a(1.0F, 1.0F, 1.0F, 1.0F / (j + 1));
            int k = this.f;
            int i1 = this.g;
            float f = (j - i / 2) / 256.0F;
            localavd.a(k, i1, this.i, 0.0F + f, 0.0D);
            localavd.a(k, 0.0D, this.i, 1.0F + f, 0.0D);
            localavd.a(0.0D, 0.0D, this.i, 1.0F + f, 1.0D);
            localavd.a(0.0D, i1, this.i, 0.0F + f, 1.0D);
        }
        localavd.a();
        GL11.glColorMask(true, true, true, true);
    }

    private void c(int paramInt1, int paramInt2, float paramFloat) {
        GL11.glViewport(0, 0, 256, 256);
        b(paramInt1, paramInt2, paramFloat);
        GL11.glDisable(3553);
        GL11.glEnable(3553);

        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        a(paramFloat);
        GL11.glViewport(0, 0, this.e.c, this.e.d);

        avd localavd = avd.a;
        localavd.b();

        float f1 = this.f > this.g ? 120.0F / this.f : 120.0F / this.g;
        float f2 = this.g * f1 / 256.0F;
        float f3 = this.f * f1 / 256.0F;
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        localavd.a(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.f;
        int j = this.g;
        localavd.a(0.0D, j, this.i, 0.5F - f2, 0.5F + f3);
        localavd.a(i, j, this.i, 0.5F - f2, 0.5F - f3);
        localavd.a(i, 0.0D, this.i, 0.5F + f2, 0.5F - f3);
        localavd.a(0.0D, 0.0D, this.i, 0.5F + f2, 0.5F + f3);
        localavd.a();
    }

    public void a(int paramInt1, int paramInt2, float paramFloat) {
        c(paramInt1, paramInt2, paramFloat);
        avd localavd = avd.a;

        int i = 274;
        int j = this.f / 2 - i / 2;
        int k = 30;

        a(0, 0, this.f, this.g, -2130706433, 16777215);
        a(0, 0, this.f, this.g, 0, -2147483648);

        GL11.glBindTexture(3553, this.e.o.b("/title/mclogo.png"));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.b < 0.0001D) {
            b(j + 0, k + 0, 0, 0, 99, 44);
            b(j + 99, k + 0, 129, 0, 27, 44);
            b(j + 99 + 26, k + 0, 126, 0, 3, 44);
            b(j + 99 + 26 + 3, k + 0, 99, 0, 26, 44);
            b(j + 155, k + 0, 0, 45, 155, 44);
        } else {
            b(j + 0, k + 0, 0, 0, 155, 44);
            b(j + 155, k + 0, 0, 45, 155, 44);
        }

        localavd.d(16777215);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.f / 2 + 90, 70.0F, 0.0F);

        GL11.glRotatef(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - ig
                .e(ig.a((float) (Minecraft.F() % 1000L) / 1000.0F * 3.141593F * 2.0F) * 0.1F);

        f = f * 100.0F / (this.k.a(this.c) + 32);
        GL11.glScalef(f, f, f);
        a(this.k, this.c, 0, -8, 16776960);
        GL11.glPopMatrix();

        String str1 = "Minecraft 1.3.1";
        if (this.e.q()) {
            str1 = str1 + " Demo";
        }

        b(this.k, str1, 2, this.g - 10, 16777215);
        String str2 = "Copyright Mojang AB. Do not distribute!";
        b(this.k, str2, this.f - this.k.a(str2) - 2, this.g - 10, 16777215);

        super.a(paramInt1, paramInt2, paramFloat);
    }
}
