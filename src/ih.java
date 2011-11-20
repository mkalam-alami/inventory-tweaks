/**
 * 
 * @author Mojang, Jimeo Wan
 *
 */
public class ih extends ia {
    public dk a;
    private int e;
    public int b = 0;
    public int c;
    private int f = 5;

    public float d = (float) (Math.random() * 3.141592653589793D * 2.0D);

    public ih(ry paramry, double paramDouble1, double paramDouble2, double paramDouble3, dk paramdk) {
        super(paramry);
        a(0.25F, 0.25F);
        this.L = (this.N / 2.0F);
        d(paramDouble1, paramDouble2, paramDouble3);
        this.a = paramdk;

        this.y = (float) (Math.random() * 360.0D);

        this.v = (float) (Math.random() * 0.2000000029802322D - 0.1000000014901161D);
        this.w = 0.2000000029802322D;
        this.x = (float) (Math.random() * 0.2000000029802322D - 0.1000000014901161D);
    }

    protected boolean d_() {
        return false;
    }

    public ih(ry paramry) {
        super(paramry);
        a(0.25F, 0.25F);
        this.L = (this.N / 2.0F);
    }

    protected void b() {
    }

    public void a() {
        super.a();
        if (this.c > 0)
            this.c -= 1;
        this.p = this.s;
        this.q = this.t;
        this.r = this.u;

        this.w -= 0.03999999910593033D;
        try {
            if (this.o.e(me.c(this.s), me.c(this.t), me.c(this.u)) == getP("h")) {
                this.w = 0.2000000029802322D;
                this.v = ((this.Y.nextFloat() - this.Y.nextFloat()) * 0.2F);
                this.x = ((this.Y.nextFloat() - this.Y.nextFloat()) * 0.2F);
                this.o.a(this, "random.fizz", 0.4F, 2.0F + this.Y.nextFloat() * 0.4F);
            }
        }
        catch (Exception e) {
            // Do nothing
        }
        c(this.s, (this.C.b + this.C.e) / 2.0D, this.u);
        b(this.v, this.w, this.x);

        float f1 = 0.98F;
        if (this.D) {
            f1 = 0.5880001F;
            int i = this.o.a(me.c(this.s), me.c(this.C.b) - 1, me.c(this.u));
            if (i > 0) {
                f1 = yy.k[i].ca * 0.98F;
            }
        }

        this.v *= f1;
        this.w *= 0.9800000190734863D;
        this.x *= f1;

        if (this.D) {
            this.w *= -0.5D;
        }

        this.e += 1;
        this.b += 1;
        if (this.b >= 6000)
            v();
    }

    public boolean h_() {
        return this.o.a(this.C, getP("g"), this);
    }

    protected void a(int paramInt) {
        a(pm.a, paramInt);
    }

    public boolean a(pm parampm, int paramInt) {
        G();
        this.f -= paramInt;
        if (this.f <= 0) {
            v();
        }
        return false;
    }

    public void a(ik paramik) {
        paramik.a("Health", (short) (byte) this.f);
        paramik.a("Age", (short) this.b);
        paramik.a("Item", this.a.b(new ik()));
    }

    public void b(ik paramik) {
        this.f = (paramik.d("Health") & 0xFF);
        this.b = paramik.d("Age");
        ik localik = paramik.k("Item");
        this.a = dk.a(localik);
        if (this.a == null)
            v();
    }

    public void a(vi paramvi) {
        if (this.o.I)
            return;

        int i = this.a.a;
        if ((this.c == 0) && (paramvi.by.a(this.a))) {
            if (this.a.c == yy.J.bM)
                paramvi.a(ut.g);
            if (this.a.c == acy.aE.bM)
                paramvi.a(ut.t);
            if (this.a.c == acy.m.bM)
                paramvi.a(ut.w);
            if (this.a.c == acy.bn.bM)
                paramvi.a(ut.z);
            
            ModLoader_InvTweaks.OnItemPickup(paramvi, this.a);
            
            this.o.a(this, "random.pop", 0.2F, ((this.Y.nextFloat() - this.Y.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            paramvi.a(this, i);

            if (this.a.a <= 0)
                v();
        }
    }

    /**
     * Hack to avoid the coflict between the p class and the p local field.
     * @return
     */
    private p getP(String staticPField) {
        try {
            return (p) Class.forName("p").getField(staticPField).get(null);
        } catch (Exception e) {
            return null;
        }
    }
}