  
 public class ms extends iw
 {
   public qs a;
   public int b = 0;
   public int c;
   private int e = 5;
 
   public float d = (float)(Math.random() * 3.141592653589793D * 2.0D);
 
   public ms(ty paramty, double paramDouble1, double paramDouble2, double paramDouble3, qs paramqs) {
     super(paramty);
     a(0.25F, 0.25F);
     this.M = (this.O / 2.0F);
     b(paramDouble1, paramDouble2, paramDouble3);
     this.a = paramqs;
 
     this.z = (float)(Math.random() * 360.0D);
 
     this.w = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
     this.x = 0.2000000029802322D;
     this.y = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
   }
 
   protected boolean e_()
   {
     return false;
   }
 
   public ms(ty paramty) {
     super(paramty);
     a(0.25F, 0.25F);
     this.M = (this.O / 2.0F);
   }
 
   protected void a()
   {
   }
 
   public void h_()
   {
     super.h_();
     if (this.c > 0) this.c -= 1;
     this.q = this.t;
     this.r = this.u;
     this.s = this.v;
 
     this.x -= 0.03999999910593033D;
     i(this.t, (this.D.b + this.D.e) / 2.0D, this.v);
     d(this.w, this.x, this.y);
 
     int i = ((int)this.q != (int)this.t) || ((int)this.r != (int)this.u) || ((int)this.s != (int)this.v) ? 1 : 0;
 
     if (i != 0) {
       if (this.p.f(hq.c(this.t), hq.c(this.u), hq.c(this.v)) == abx.h) {
         this.x = 0.2000000029802322D;
         this.w = ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.2F);
         this.y = ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.2F);
         this.p.a(this, "random.fizz", 0.4F, 2.0F + this.Z.nextFloat() * 0.4F);
       }
 
       if (!this.p.K) {
         for (Object localms : this.p.a(ms.class, this.D.b(0.5D, 0.0D, 0.5D))) {
           a((ms) localms);
         }
       }
     }
 
     float f = 0.98F;
     if (this.E) {
       f = 0.5880001F;
       int j = this.p.a(hq.c(this.t), hq.c(this.D.b) - 1, hq.c(this.v));
       if (j > 0) {
         f = ahp.m[j].cq * 0.98F;
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
 
   public boolean a(ms paramms)
   {
     if (paramms == this) return false;
     if ((!paramms.S()) || (!S())) return false;
     if (paramms.a.b() != this.a.b()) return false;
     if ((paramms.a.b().k()) && (paramms.a.j() != this.a.j())) return false;
     if (paramms.a.a < this.a.a) return paramms.a(this);
     if (paramms.a.a + this.a.a > paramms.a.d()) return false;
 
     paramms.a.a += this.a.a;
     paramms.c = Math.max(paramms.c, this.c);
     paramms.b = Math.min(paramms.b, this.b);
     y();
 
     return true;
   }
 
   public void d()
   {
     this.b = 4800;
   }
 
   public boolean I()
   {
     return this.p.a(this.D, abx.g, this);
   }
 
   protected void e(int paramInt)
   {
     a(in.a, paramInt);
   }
 
   public boolean a(in paramin, int paramInt)
   {
     K();
     this.e -= paramInt;
     if (this.e <= 0) {
       y();
     }
     return false;
   }
 
   public void b(an paraman)
   {
     paraman.a("Health", (short)(byte)this.e);
     paraman.a("Age", (short)this.b);
     if (this.a != null) paraman.a("Item", this.a.b(new an()));
   }
 
   public void a(an paraman)
   {
     this.e = (paraman.d("Health") & 0xFF);
     this.b = paraman.d("Age");
     an localan = paraman.l("Item");
     this.a = qs.a(localan);
     if (this.a == null) y();
   }
 
   public void b_(np paramnp)
   {
     if (this.p.K) return;
 
     int i = this.a.a;
     if ((this.c == 0) && (paramnp.by.a(this.a))) {
       if (this.a.c == ahp.J.ca) paramnp.a(gu.g);
       if (this.a.c == qq.aF.bT) paramnp.a(gu.t);
       if (this.a.c == qq.n.bT) paramnp.a(gu.w);
       if (this.a.c == qq.bo.bT) paramnp.a(gu.z);
       
       // InvTweaks on pickup hook
       ModLoader_InvTweaks.onItemPickup(paramnp, this.a);
       
       this.p.a(this, "random.pop", 0.2F, ((this.Z.nextFloat() - this.Z.nextFloat()) * 0.7F + 1.0F) * 2.0F);
       paramnp.a(this, i);
       if (this.a.a <= 0) y();
     }
   }
 
   public String ak()
   {
       return "InvTweaks4Ev3r"; // FIXME
     //  return aj.a("item." + this.a.a());
   }
 
   public boolean an()
   {
     return false;
   }
 }

