---
---

## Generators
---

Generators are the work-horses of Orchid, and form the basis around which everything else in Orchid lives. Each 
Generator is kind-of like a mini static site generator all on its own, except that it is able to work with all the other
generators in your build to produce a site that is flexible and extensible in ways that no other tool could ever be. 

Generators primarily have two jobs: to decide what Pages should exist for a particular section of the site, and how we 
need to process content to create those pages. Generators work in two phases: the Indexing phase, and the Generation 
phase, which will de described further below. Generators create a logical grouping of content within your site, and are
usually set up to be minimal and do just one job very well, rather than attempting to do everything itself.
 
As one build typically has many generators, each with a focused task, and each one can be turned on or off at will, 
you'll be able to pick-and-choose the exact set of features that your site needs without worrying about bloating it with 
stuff you don't need.

A familiar example for what exactly a generator is, is the {{anchor('Orchid Posts')}} generator, which creates blog 
posts with customizable permalinks and orders its pages by publication date. The Posts generator does not generate any
archives of its own, but instead the {{anchor('Orchid Taxonomies')}} generator does that job for us. The Taxonomies 
generator can inspect all the blog posts being created by the Posts generator, and can generate archives for those 
pages. Because both the Posts and Taxonomies generators are small and focused, they can be easily composed or used with
other plugins as well, or completely omitted if you find you don't want a blog or need full archives. 

Most plugins are centered around building a single generator, along with {{anchor('Components')}} and 
{{anchor('menus')}} and other features that expand upon the content from that generator..

### Indexing Phase

During the Indexing phase, Orchid asks each Generator, in turn, for a list of Pages it intends to produce. The methods
that each generator uses to determine these pages is irrelevant; all Orchid cares about is the end result, which is that
there are some Pages that need to be rendered into the output site. 

It is common for Generators to create Pages that correspond directly to a source file, such as with Blog posts, Static
Pages, or the Wiki you're currently reading. Another common occurrence is for some external program to parse a content
model from source code, which is then integrated into Orchid. Such is the case with the Orchid Javadoc and KSS plugins. 

Generators may also index content that is not intended to be a page in the output, but as the content for a component or 
menu. Such content is typically smaller in scope but used on many pages. An example would be the Orchid Forms and 
Presentations plugins. Each Form has a non-trivial configuration, and a single form may be used across many pages. By 
indexing the form definition in a Generator, we can be sure that all forms of a given type are always kept in sync
throughout the site, and are immune to changes not propagating.

### Generating Phase

After all Generators have finished indexing their content, Orchid does some processing on these output Pages before 
passing them back to their source Generator to be rendered. Pages can be rendered in several ways: into a Layout 
template resource, into a String layout, as raw content, and as a binary stream. 

**Rendering as Layout**

When rendering a page as a template, Orchid asks the Page what layout it should use. Orchid then attempts to locate that
layout in the theme or in the local resources directory, which takes precedence over the theme-defined layouts and allow
for themes to be customized without needing access to the theme files itself. 

**Rendering as String**

Rendering a stream is similar to rendering as a layout, except that the template definition comes as a static String 
rather than an override-able resource. This is typically more useful in testing, but may be used in specific situations
if the Generator calls for a very specific layout and doesn't want to allow it to be changed.

**Rendering Raw**

Many types of pages should not be rendered into a layout but should still have its content processed, such as compiling 
SCSS into CSS. The content of these Pages is loaded and processed as a String.

**Render Binary**

Other pages will become corrupted if the source content is loaded as a String, and must be treated as a stream of bytes,
such as images, videos, or PDF files. The content of these pages are not processed at all, and are simply copied 
directly from the source to the destination.

Pages may be set as a draft, which will skip the rendering of these pages. This is done behind the scenes and is not
a concern of the Generator. The Generator should simply render all pages passed to it as if it should be rendered.

Generators can also set a new theme to be used just for its own pages, which completely changes the layouts, CSS/JS, 
menus, etc. that are loaded for these pages.

## Configuring Generators
---

Generators are configured in your `config.yml`. There are a few options in common amongst all generators, but most 
generators will define their own options as well.

Each generator has a unique key, and all the options for a generator are defined under that one key. 

{% highlight 'yaml' %}
# config.yml
posts:
  categories: 
    - 'personal'
    - 'programming'
  authors:
    - 'Casey Brooks'
{% endhighlight %}

Most generators will do just fine if you don't configure them at all, as they are set up with sensible default values. 
