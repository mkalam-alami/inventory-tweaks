/*     */ /*     */ 
/*     */ public class pf extends la
/*     */ {
/*     */   public tu a;
/*  23 */   public int b = 0;
/*     */   public int c;
/*  26 */   private int e = 5;
/*     */ 
/*  28 */   public float d = (float)(Math.random() * 3.141592653589793D * 2.0D);
/*     */ 
/*     */   public pf(xd paramxd, double paramDouble1, double paramDouble2, double paramDouble3, tu paramtu) {
/*  31 */     super(paramxd);
/*  32 */     a(0.25F, 0.25F);
/*  33 */     this.M = (this.O / 2.0F);
/*  34 */     b(paramDouble1, paramDouble2, paramDouble3);
/*  35 */     this.a = paramtu;
/*     */ 
/*  37 */     this.z = (float)(Math.random() * 360.0D);
/*     */ 
/*  39 */     this.w = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
/*  40 */     this.x = 0.2000000029802322D;
/*  41 */     this.y = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
/*     */   }
/*     */ 
/*     */   protected boolean f_()
/*     */   {
/*  46 */     return false;
/*     */   }
/*     */ 
/*     */   public pf(xd paramxd) {
/*  50 */     super(paramxd);
/*  51 */     a(0.25F, 0.25F);
/*  52 */     this.M = (this.O / 2.0F);
/*     */   }
/*     */ 
/*     */   protected void a()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void j_()
/*     */   {
/*  61 */     super.j_();
/*  62 */     if (this.c > 0) this.c -= 1;
/*  63 */     this.q = this.t;
/*  64 */     this.r = this.u;
/*  65 */     this.s = this.v;
/*     */ 
/*  67 */     this.x -= 0.03999999910593033D;
/*  68 */     i(this.t, (this.D.b + this.D.e) / 2.0D, this.v);
/*  69 */     d(this.w, this.x, this.y);
/*     */ 
/*  71 */     int i = ((int)this.q != (int)this.t) || ((int)this.r != (int)this.u) || ((int)this.s != (int)this.v) ? 1 : 0;
/*     */ 
/*  73 */     if (i != 0) {
/*  74 */       if (this.p.f(ju.c(this.t), ju.c(this.u), ju.c(this.v)) == aff.i) {
/*  75 */         this.x = 0.2000000029802322D;
/*  76 */         this.w = ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
/*  77 */         this.y = ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.2F);
/*  78 */         this.p.a(this, "random.fizz", 0.4F, 2.0F + this.aa.nextFloat() * 0.4F);
/*     */       }
/*     */ 
/*  81 */       if (!this.p.J) {
/*  82 */         for (Object localpf : this.p.a(pf.class, this.D.b(0.5D, 0.0D, 0.5D))) {
/*  83 */           a((pf) localpf);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  88 */     float f = 0.98F;
/*  89 */     if (this.E) {
/*  90 */       f = 0.5880001F;
/*  91 */       int j = this.p.a(ju.c(this.t), ju.c(this.D.b) - 1, ju.c(this.v));
/*  92 */       if (j > 0) {
/*  93 */         f = ale.p[j].cC * 0.98F;
/*     */       }
/*     */     }
/*     */ 
/*  97 */     this.w *= f;
/*  98 */     this.x *= 0.9800000190734863D;
/*  99 */     this.y *= f;
/*     */ 
/* 101 */     if (this.E) {
/* 102 */       this.x *= -0.5D;
/*     */     }
/*     */ 
/* 105 */     this.b += 1;
/* 106 */     if ((!this.p.J) && (this.b >= 6000))
/* 107 */       x();
/*     */   }
/*     */ 
/*     */   public boolean a(pf parampf)
/*     */   {
/* 112 */     if (parampf == this) return false;
/* 113 */     if ((!parampf.S()) || (!S())) return false;
/* 114 */     if (parampf.a.b() != this.a.b()) return false;
/* 115 */     if ((parampf.a.o()) || (this.a.o())) return false;
/* 116 */     if ((parampf.a.b().l()) && (parampf.a.j() != this.a.j())) return false;
/* 117 */     if (parampf.a.a < this.a.a) return parampf.a(this);
/* 118 */     if (parampf.a.a + this.a.a > parampf.a.d()) return false;
/*     */ 
/* 120 */     parampf.a.a += this.a.a;
/* 121 */     parampf.c = Math.max(parampf.c, this.c);
/* 122 */     parampf.b = Math.min(parampf.b, this.b);
/* 123 */     x();
/*     */ 
/* 125 */     return true;
/*     */   }
/*     */ 
/*     */   public void c()
/*     */   {
/* 130 */     this.b = 4800;
/*     */   }
/*     */ 
/*     */   public boolean I()
/*     */   {
/* 135 */     return this.p.a(this.D, aff.h, this);
/*     */   }
/*     */ 
/*     */   protected void d(int paramInt)
/*     */   {
/* 140 */     a(kr.a, paramInt);
/*     */   }
/*     */ 
/*     */   public boolean a(kr paramkr, int paramInt)
/*     */   {
/* 145 */     K();
/* 146 */     this.e -= paramInt;
/* 147 */     if (this.e <= 0) {
/* 148 */       x();
/*     */     }
/* 150 */     return false;
/*     */   }
/*     */ 
/*     */   public void b(bg parambg)
/*     */   {
/* 155 */     parambg.a("Health", (short)(byte)this.e);
/* 156 */     parambg.a("Age", (short)this.b);
/* 157 */     if (this.a != null) parambg.a("Item", this.a.b(new bg()));
/*     */   }
/*     */ 
/*     */   public void a(bg parambg)
/*     */   {
/* 162 */     this.e = (parambg.d("Health") & 0xFF);
/* 163 */     this.b = parambg.d("Age");
/* 164 */     bg localbg = parambg.l("Item");
/* 165 */     this.a = tu.a(localbg);
/* 166 */     if (this.a == null) x();
/*     */   }
/*     */ 
/*     */   public void b_(qf paramqf)
/*     */   {
/* 171 */     if (this.p.J) return;
/*     */ 
/* 173 */     int i = this.a.a;
/* 174 */     if ((this.c == 0) && (paramqf.bJ.a(this.a))) {
/* 175 */       if (this.a.c == ale.M.cm) paramqf.a(ix.g);
/* 176 */       if (this.a.c == ts.aF.cf) paramqf.a(ix.t);
/* 177 */       if (this.a.c == ts.n.cf) paramqf.a(ix.w);
/* 178 */       if (this.a.c == ts.bo.cf) paramqf.a(ix.z);

                // InvTweaks on pickup hook
                ModLoader_InvTweaks.onItemPickup(paramqf, this.a);
                
/* 179 */       this.p.a(this, "random.pop", 0.2F, ((this.aa.nextFloat() - this.aa.nextFloat()) * 0.7F + 1.0F) * 2.0F);
/* 180 */       paramqf.a(this, i);
/* 181 */       if (this.a.a <= 0) x();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String an()
/*     */   {
/* 187 */     return bc.a("item." + this.a.a());
/*     */   }
/*     */ 
/*     */   public boolean aq()
/*     */   {
/* 192 */     return false;
/*     */   }
/*     */ }

/* Location:           D:\Développement\Minecraft\sources\minecraft14RC.bin\minecraft.jar
 * Qualified Name:     pf
 * JD-Core Version:    0.6.0
 */