package org.jenkins.plugins.all_changes.AllChangesAction

import com.google.common.collect.Multimap
import hudson.Functions
import hudson.model.AbstractBuild
import hudson.model.AbstractBuild.DependencyChange
import hudson.scm.ChangeLogSet
import java.text.DateFormat
import org.apache.commons.jelly.XMLOutput
import org.dom4j.io.SAXContentHandler
import org.jvnet.localizer.LocaleProvider

f=namespace(lib.FormTagLib)
l=namespace(lib.LayoutTagLib)
t=namespace("/lib/hudson")
st=namespace("jelly:stapler")

private def wrapOutput(Closure viewInstructions) {
  def sc = new SAXContentHandler()
  def old = setOutput(new XMLOutput(sc))
  viewInstructions();
  setOutput(old);
  return sc
}

l.layout(title: _("All Changes")) {
  st.include(page: "sidepanel.jelly", it: my.project)
  l.main_panel() {
    def from = request.getParameter('from')
    def to = request.getParameter('to')

    h1(_("All Changes"))
    def builds = Functions.filter(my.project.buildsAsMap,from,to).values()
    for (build in builds) {
      Multimap<ChangeLogSet.Entry, AbstractBuild> changes = my.getAllChanges(build);
      if (changes.empty) {
        continue
      }
      h2() {
        a(href:"${my.project.absoluteUrl}/${build.number}/changes",
                "${build.displayName}  (${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, LocaleProvider.locale).format(build.timestamp.time)})")
      }
      ol() {
        for (entry in changes.keySet()) {
          li() {
            showChangeSet(entry)
            boolean firstDrawn = false
            for (AbstractBuild b in changes.get(entry)) {
              if (b != build) {
                if (!firstDrawn) {
                  text(" (")
                  firstDrawn = true
                }
                else {
                  text(", ")
                }
                a(href: "${rootURL}/${b.project.url}") {text(b.project.displayName)}
                st.nbsp()
                a(href: "${rootURL}/${b.url}") {
                  text(b.displayName)
                }
              }
            }
            if (firstDrawn) {
              text(")")
            }

          }
        }
      }
    }
  }
}

private def showChangeSet(ChangeLogSet.Entry c) {
  def build = c.parent.build
  def browser = build.project.scm.effectiveBrowser
  text(c.msgAnnotated)
  raw(" &#8212; ")
  if (browser?.getChangeSetLink(c)) {
    a(href: browser.getChangeSetLink(c), _("detail"))
  } else {
    a(href: "${build.absoluteUrl}changes", _("detail"))
  }
}

private def showDependencyChanges(DependencyChange dep) {
  a(href: "${rootURL}/${dep.project.url}") {text(dep.project.displayName)}
  st.nbsp()
  a(href: "${rootURL}/${dep.from.url}") {
    delegate.img(src: "${imagesURL}/16x16/${dep.from.buildStatusUrl}",
            alt: "${dep.from.iconColor.description}", height: "16", width: "16")
    text(dep.from.displayName)
  }

  raw("&#x2192;") // right arrow
  a(href: "${rootURL}/${dep.to.url}") {
    delegate.img(src: "${imagesURL}/16x16/${dep.to.buildStatusUrl}",
            alt: "${dep.to.iconColor.description}", height: "16", width: "16")
    text(dep.to.displayName)
  }
}
