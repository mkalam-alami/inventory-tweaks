public class ni extends jm {
    
    public ri a;
    public int b = 0;
    public int c;
    private int e = 5;

    public float d = (float) (Math.random() * 3.141592653589793D * 2.0D);

    public ni(uo paramuo, double paramDouble1, double paramDouble2, double paramDouble3, ri paramri) {
        super(paramuo);
        a(0.25F, 0.25F);
        this.M = (this.O / 2.0F);
        b(paramDouble1, paramDouble2, paramDouble3);
        this.a = paramri;

        this.z = (float) (Math.random() * 360.0D);

        this.w = (float) (Math.random() * 0.2000000029802322D - 0.1000000014901161D);
        this.x = 0.2000000029802322D;
        this.y = (float) (Math.random() * 0.2000000029802322D - 0.1000000014901161D);
    }

    protected boolean e_() {
        return false;
    }

    public ni(uo paramuo) {
        super(paramuo);
        a(0.25F, 0.25F);
        this.M = (this.O / 2.0F);
    }

    protected void a() {
    }

    public void h_() {
        super.h_();
        if (this.c > 0)
            this.c -= 1;
        this.q = this.t;
        this.r = this.u;
        this.s = this.v;

        this.x -= 0.03999999910593033D;
        i(this.t, (this.D.b + this.D.e) / 2.0D, this.v);
        d(this.w, this.x, this.y);

        int i = ((int) this.q != (int) this.t) || ((int) this.r != (int) this.u)
                || ((int) this.s != (int) this.v) ? 1 : 0;

        if (i != 0) {
            if (this.p.f(ig.c(this.t), ig.c(this.u), ig.c(this.v)) == acn.h) {
                this.x = 0.2000000029802322D;
                this.w = ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.2F);
                this.y = ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.2F);
                this.p.a(this, "random.fizz", 0.4F, 2.0F + this.Z.nextFloat() * 0.4F);
            }

            if (!this.p.K) {
                for (Object localni : this.p.a(ni.class, this.D.b(0.5D, 0.0D, 0.5D))) {
                    a((ni) localni);
                }
            }
        }

        float f = 0.98F;
        if (this.E) {
            f = 0.5880001F;
            int j = this.p.a(ig.c(this.t), ig.c(this.D.b) - 1, ig.c(this.v));
            if (j > 0) {
                f = aif.m[j].cq * 0.98F;
            }
        }

        this.w *= f;
        this.x *= 0.9800000190734863D;
        this.y *= f;

        if (this.E) {
            this.x *= -0.5D;
        }

        this.b += 1;
        if (this.b >= 6000)
            y();
    }

    public boolean a(ni paramni) {
        if (paramni == this)
            return false;
        if ((!paramni.S()) || (!S()))
            return false;
        if (paramni.a.b() != this.a.b())
            return false;
        if ((paramni.a.b().k()) && (paramni.a.j() != this.a.j()))
            return false;
        if (paramni.a.a < this.a.a)
            return paramni.a(this);
        if (paramni.a.a + this.a.a > paramni.a.d())
            return false;

        paramni.a.a += this.a.a;
        paramni.c = Math.max(paramni.c, this.c);
        paramni.b = Math.min(paramni.b, this.b);
        y();

        return true;
    }

    public void d() {
        this.b = 4800;
    }

    public boolean I() {
        return this.p.a(this.D, acn.g, this);
    }

    protected void e(int paramInt) {
        a(jd.a, paramInt);
    }

    public boolean a(jd paramjd, int paramInt) {
        K();
        this.e -= paramInt;
        if (this.e <= 0) {
            y();
        }
        return false;
    }

    public void b(an paraman) {
        paraman.a("Health", (short) (byte) this.e);
        paraman.a("Age", (short) this.b);
        if (this.a != null)
            paraman.a("Item", this.a.b(new an()));
    }

    public void a(an paraman) {
        this.e = (paraman.d("Health") & 0xFF);
        this.b = paraman.d("Age");
        an localan = paraman.l("Item");
        this.a = ri.a(localan);
        if (this.a == null)
            y();
    }

    public void b_(of paramof) {
        if (this.p.K)
            return;

        int i = this.a.a;
        if ((this.c == 0) && (paramof.by.a(this.a))) {
            if (this.a.c == aif.J.ca)
                paramof.a(hk.g);
            if (this.a.c == rg.aF.bT)
                paramof.a(hk.t);
            if (this.a.c == rg.n.bT)
                paramof.a(hk.w);
            if (this.a.c == rg.bo.bT)
                paramof.a(hk.z);

            // InvTweaks on pickup hook
            ModLoader_InvTweaks.onItemPickup(paramof, this.a);

            this.p.a(this, "random.pop", 0.2F,
                    ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            paramof.a(this, i);
            if (this.a.a <= 0)
                y();
        }
    }

    public String ak() {
        return "item." + this.a.a(); // aj.a("item." + this.a.a());
    }

    public boolean an() {
        return false;
    }
}
